const { test, before, after } = require('node:test');
const assert = require('node:assert/strict');
const path = require('node:path');
const grpc = require('@grpc/grpc-js');
const loader = require('@grpc/proto-loader');

// Servidores gRPC aislados en puertos libres: nunca contactan la base real.
const servidores = [];
let http, url, conexiones;
let reservasCaidas = false;
let respuestaNueva = false;
const solicitudesClientes = [];
const usuarios = [
  { id: '1', dni: '11111111', nombre: 'Ana', apellido: 'Uno', rol: 'CLIENTE' },
  { id: '2', dni: '22222222', nombre: 'Juan', apellido: 'Dos', rol: 'CLIENTE' },
  { id: '3', dni: '33333333', nombre: 'Admin', apellido: 'Prueba', rol: 'ADMIN' }
];
const vehiculo = { id: '1', marca: 'Fiat', modelo: 'Cronos', patente: 'PRUEBA01',
  tipoVehiculo: 'SEDAN', precioDiario: 25000, estaActivo: true, estado: 'DISPONIBLE', anio: 2024, color: 'Blanco' };
const reservas = [
  { idReserva: '101', idCliente: '1', patenteVehiculo: 'PRUEBA01', fechaHoraInicio: '2026-11-10T10:00:00', fechaHoraFinal: '2026-11-12T10:00:00', estadoReserva: 'CONFIRMADO', importeTotal: 50000 },
  { idReserva: '102', idCliente: '2', patenteVehiculo: 'PRUEBA01', fechaHoraInicio: '2026-12-10T10:00:00', fechaHoraFinal: '2026-12-12T10:00:00', estadoReserva: 'CONFIRMADO', importeTotal: 50000 }
];
async function servicio(archivo, paquete, nombre, implementacion) {
  const proto = grpc.loadPackageDefinition(loader.loadSync(path.resolve(__dirname, '../../proto/contratos', archivo), {
    keepCase: false, longs: String, enums: String, defaults: true, oneofs: true
  }));
  const servidor = new grpc.Server();
  servidor.addService(proto[paquete][nombre].service, implementacion);
  const port = await new Promise((resolve, reject) => servidor.bindAsync('127.0.0.1:0', grpc.ServerCredentials.createInsecure(), (e, p) => e ? reject(e) : resolve(p)));
  servidores.push(servidor);
  return `127.0.0.1:${port}`;
}
before(async () => {
  process.env.CLIENTES_GRPC_ADDR = await servicio('clientes/clientes.proto', 'clientes', 'ClienteService', {
    validarLogin(call, cb) {
      const u = usuarios.find(u => u.id === call.request.usuario);
      if (!u || call.request.password !== 'ficticia') return cb({ code: grpc.status.UNAUTHENTICATED });
      cb(null, { id: String(100 + Number(u.id)), usuario: u.id, rol: u.rol, password: "hash-ficticio-no-exponer" });
    },
    listarUsuarios(call, cb) {
      cb(null, { cliente: usuarios.map(u => ({ ...u, estaActivo: true, metadatos: { id: String(100 + Number(u.id)), usuario: u.id, rol: u.rol, password: "hash-ficticio-no-exponer" } })) });
    },
    consultarClienteId(call, cb) {
      solicitudesClientes.push(call.request.id);
      const cliente = usuarios.find(u => u.id === call.request.id);
      cb(null, respuestaNueva ? { cliente } : { status: 'SUCCESS', cliente });
    }
  });
  process.env.VEHICULOS_GRPC_ADDR = await servicio('vehiculos/vehiculo.proto', 'vehiculos', 'VehiculoService', {
    getVehiculoByPatente(call, cb) { cb(null, vehiculo); },
    getVehiculoById(call, cb) { cb(null, vehiculo); },
    getAllVehiculos(call, cb) { cb(null, { vehiculos: [vehiculo] }); }
  });
  process.env.RESERVAS_GRPC_ADDR = await servicio('reservas/reserva.proto', 'reservas', 'ReservaService', {
    listarReservas(call, cb) { reservasCaidas ? cb({ code: grpc.status.UNAVAILABLE }) : cb(null, { reservas }); },
    obtenerReserva(call, cb) {
      const r = reservas.find(r => r.idReserva === call.request.id);
      r ? cb(null, r) : cb({ code: grpc.status.NOT_FOUND });
    },
    cancelarReserva(call, cb) { cb(null, { exito: true, estadoReserva: 'CANCELADO' }); },
    obtenerMiHistorial(call, cb) { cb(null, { historial: [] }); }
  });
  const app = require('../src/server');
  conexiones = require('../src/grpc-clients');
  http = app.listen(0, '127.0.0.1');
  await new Promise(resolve => http.once('listening', resolve));
  url = `http://127.0.0.1:${http.address().port}`;
});
after(async () => {
  conexiones?.cerrarClientes();
  if (http) await new Promise(resolve => http.close(resolve));
  servidores.forEach(s => s.forceShutdown());
});
async function request(ruta, { cookie, body, method = body ? 'POST' : 'GET' } = {}) {
  const response = await fetch(url + ruta, { method, headers: {
    ...(cookie ? { Cookie: cookie } : {}), ...(body ? { 'Content-Type': 'application/json' } : {})
  }, body: body ? JSON.stringify(body) : undefined });
  const raw = await response.text();
  return { status: response.status, cookie: response.headers.get('set-cookie')?.split(';')[0],
    data: response.headers.get('content-type')?.includes('json') ? JSON.parse(raw) : raw };
}
async function login(id) {
  const r = await request('/auth/login', { body: { usuario: id, password: 'ficticia' } });
  assert.equal(r.status, 200);
  assert.ok(r.cookie);
  return r.cookie;
}
const consulta = 'query($f: FiltroReservasInput) { reservas(filtros: $f) { id cliente vehiculo patente estado importeTotal } }';
const consultar = (cookie, f) => request('/graphql', { cookie, body: { query: consulta, variables: { f } } });

test('API sin sesión rechazada y contraseña incorrecta rechazada', async () => {
  assert.equal((await request('/auth/me')).status, 401);
  assert.equal((await consultar()).status, 401);
  assert.equal((await request('/auth/login', { body: { usuario: '1', password: 'incorrecta' } })).status, 401);
});
test('GraphQL publica reservas y combina los tres servicios sin exponer otros clientes', async () => {
  solicitudesClientes.length = 0;
  const cookie = await login('1');
  const r = await consultar(cookie, { dniCliente: '22222222' });
  assert.equal(r.status, 200);
  assert.equal(r.data.errors, undefined);
  assert.deepEqual(r.data.data.reservas.map(r => r.id), ['101']);
  assert.equal(r.data.data.reservas[0].cliente, 'Ana Uno');
  assert.equal(r.data.data.reservas[0].vehiculo, 'Fiat Cronos');
  assert.deepEqual(solicitudesClientes, ['1']);
});
test('cada cliente recibe únicamente sus reservas', async () => {
  const r = await consultar(await login('2'));
  assert.deepEqual(r.data.data.reservas.map(r => r.id), ['102']);
});
test('admin lista ordenada, filtros por DNI, fechas, tipo, patente y estado', async () => {
  const cookie = await login('3');
  assert.deepEqual((await consultar(cookie)).data.data.reservas.map(r => r.id), ['102', '101']);
  const r = await consultar(cookie, { dniCliente: '11111111', tipoVehiculo: 'SEDAN', patenteVehiculo: 'PRUEBA01', estado: 'CONFIRMADO', fechaDesde: '2026-11-11T00:00', fechaHasta: '2026-11-11T23:59:59' });
  assert.deepEqual(r.data.data.reservas.map(r => r.id), ['101']);
  assert.deepEqual((await consultar(cookie, { estado: 'CANCELADO' })).data.data.reservas, []);
  assert.deepEqual((await consultar(cookie, { tipoVehiculo: 'SUV' })).data.data.reservas, []);
  assert.deepEqual((await consultar(cookie, { patenteVehiculo: 'OTRA' })).data.data.reservas, []);
});
test('respuesta nueva de clientes sin status también se admite', async () => {
  respuestaNueva = true;
  try { assert.equal((await consultar(await login('1'))).data.data.reservas[0].id, '101'); }
  finally { respuestaNueva = false; }
});
test('fechas inválidas o invertidas producen error explícito', async () => {
  const cookie = await login('1');
  for (const f of [{ fechaDesde: '2026-02-30T00:00' }, { fechaDesde: 'mañana' }, { fechaDesde: '2026-12-01T00:00', fechaHasta: '2026-11-01T00:00' }]) {
    assert.equal((await consultar(cookie, f)).data.errors[0].extensions.code, 'BAD_USER_INPUT');
  }
});
test('fallo del servicio devuelve error y no una lista vacía engañosa', async () => {
  const cookie = await login('1');
  reservasCaidas = true;
  try { assert.equal((await consultar(cookie)).data.errors[0].extensions.code, 'SERVICIO_RESERVAS_ERROR'); }
  finally { reservasCaidas = false; }
});
test('no permite crear ni cancelar reservas de otro cliente', async () => {
  const cookie = await login('1');
  assert.equal((await request('/api_rest/v1/reservas', { cookie, body: { idCliente: '2', idVehiculo: '1' } })).status, 403);
  assert.equal((await request('/api_rest/v1/reservas/102', { cookie, method: 'PUT' })).status, 403);
});
test('historial sigue publicado y logout invalida la cookie anterior', async () => {
  const cookie = await login('1');
  const r = await request('/graphql', { cookie, body: { query: '{ miHistorialAlquileres { patente estado } }' } });
  assert.deepEqual(r.data.data.miHistorialAlquileres, []);
  assert.equal((await request('/auth/logout', { cookie, method: 'POST' })).status, 200);
  assert.equal((await request('/auth/me', { cookie })).status, 401);
});

test('login usa ID del cliente aunque el ID de metadatos sea distinto y no expone claves', async () => {
  const cookie = await login('1');
  const r = await request('/auth/me', { cookie });
  assert.equal(r.status, 200);
  assert.equal(r.data.usuario.id, '1');
  assert.equal(r.data.usuario.rol, 'CLIENTE');
  assert.equal(r.data.usuario.password, undefined);
  assert.doesNotMatch(JSON.stringify(r.data), /hash-ficticio/);
});
