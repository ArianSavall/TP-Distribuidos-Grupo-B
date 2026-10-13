const { test, before, after } = require('node:test');
const assert = require('node:assert/strict');
const { iniciarSimuladores } = require('./simuladores');
let mocks, server, url, conexiones, admin, cliente;
async function req(ruta, cookie, method = 'GET', body) {
  const r = await fetch(url + ruta, { method, redirect: 'manual', headers: {
    ...(cookie ? { Cookie: cookie } : {}), ...(body !== undefined ? { 'Content-Type': 'application/json' } : {})
  }, body: body === undefined ? undefined : JSON.stringify(body) });
  const raw = await r.text();
  return { status: r.status, location: r.headers.get('location'), cookie: r.headers.get('set-cookie')?.split(';')[0], data: r.headers.get('content-type')?.includes('json') ? JSON.parse(raw) : raw };
}
async function login(usuario) {
  const r = await req('/auth/login', null, 'POST', { usuario, password: 'clave-prueba' });
  assert.equal(r.status, 200);return r.cookie;
}
before(async () => {
  mocks = await iniciarSimuladores();Object.assign(process.env, mocks.direcciones);
  const app = require('../src/server');conexiones = require('../src/grpc-clients');
  server = app.listen(0, '127.0.0.1');await new Promise(r => server.once('listening', r));
  url = `http://127.0.0.1:${server.address().port}`;
  admin = await login('admin_prueba');cliente = await login('cliente_prueba');
});
after(async () => { conexiones?.cerrarClientes();if (server) await new Promise(r => server.close(r));mocks?.cerrar(); });
test('pantallas originales adaptadas, redirección al login y permisos de administración', async () => {
  assert.equal((await req('/')).location, '/login');
  for (const ruta of ['/', '/clientes', '/vehiculos', '/reservas', '/clientes/editar/1', '/vehiculos/editar/PRUEBA01']) {
    const r = await req(ruta, admin);assert.equal(r.status, 200, ruta);assert.match(r.data, /\/assets\/app.js/);assert.doesNotMatch(r.data, /\sth:|\ssec:/);
  }
  assert.equal((await req('/historial', cliente)).status, 200);
  assert.equal((await req('/historial', admin)).status, 403);
  for (const ruta of ['/clientes','/vehiculos','/api_rest/v1/clientes']) assert.equal((await req(ruta, cliente)).status, 403);
});
test('cliente no puede ejecutar operaciones administrativas REST', async () => {
  for (const [ruta, method] of [['/api_rest/v1/vehiculos/create','POST'],['/api_rest/v1/vehiculos/desactivar','POST'],['/api_rest/v1/vehiculos/activar','POST'],['/api_rest/v1/vehiculos/PRUEBA01','PUT'],['/api_rest/v1/clientes','POST'],['/api_rest/v1/clientes/modificar/id/1','PATCH'],['/api_rest/v1/clientes/borrar/id/2','DELETE']]) {
    assert.equal((await req(ruta, cliente, method, {})).status, 403, ruta);
  }
});
test('gestión de clientes usa gRPC, ignora rol del navegador y no devuelve contraseñas', async () => {
  const base = '/api_rest/v1/clientes';
  const nuevo = { dni: '11112222', nombre: 'Prueba', apellido: 'Alta', email: 'alta@example.com', fechaNacimiento: '2000-01-01', metadatos: { usuario: 'alta_prueba', password: 'sólo-ficticia', rol: 'ADMIN' } };
  const r = await req(base, admin, 'POST', nuevo);assert.equal(r.status, 200);assert.equal(r.data.metadatos.rol, 'CLIENTE');assert.equal(r.data.metadatos.password, undefined);
  assert.equal((await req(base, admin, 'POST', nuevo)).status, 409);
  const editado = await req(`${base}/modificar/id/${r.data.id}`, admin, 'PATCH', { nombre: 'Modificado' });
  assert.equal(editado.data.nombre, 'Modificado');
  assert.equal((await req(`${base}/modificar/dni`, admin, 'PATCH', { dni: nuevo.dni, apellido: 'Nuevo' })).data.apellido, 'Nuevo');
  assert.equal((await req(`${base}/buscar/dni/${nuevo.dni}`, admin)).data.id, r.data.id);
  assert.equal((await req(`${base}/borrar/dni/${nuevo.dni}`, admin, 'DELETE')).status, 200);
  assert.equal((await req(`${base}/buscar/id/${r.data.id}`, admin)).data.estaActivo, false);
});
test('alta y baja de vehículos y rechazo seguro de contratos incompletos', async () => {
  const base = '/api_rest/v1/vehiculos';
  const datos = { patente: 'TEST999', marca: 'Test', modelo: 'Mock', anio: 2025, tipoVehiculo: 'SUV', precioDiario: 12000, color: 'Azul' };
  assert.equal((await req(`${base}/create`, admin, 'POST', datos)).status, 200);
  assert.equal((await req(`${base}/TEST999`, admin)).data.modelo, 'Mock');
  assert.equal((await req(`${base}/TEST999`, admin, 'PUT', { modelo: 'No debe cambiar' })).status, 501);
  assert.equal((await req(`${base}/TEST999`, admin)).data.modelo, 'Mock');
  assert.equal((await req(`${base}/desactivar`, admin, 'POST', { patente: 'TEST999' })).data.estaActivo, false);
  assert.equal((await req(`${base}/activar`, admin, 'POST', { patente: 'TEST999' })).status, 501);
});
test('disponibilidad GraphQL transmite fechas, tipo y precios al servicio', async () => {
  const query = `query($max:Float) { vehiculosDisponibles(fechaInicio:"2035-01-10T10:00", fechaFinal:"2035-01-12T10:00", tipo:SEDAN, precioMax:$max) { patente marca modelo tipoVehiculo precioDiario } }`;
  const r = await req('/graphql', cliente, 'POST', { query, variables: { max: 30000 } });
  assert.equal(r.data.errors, undefined);assert.deepEqual(r.data.data.vehiculosDisponibles.map(v => v.patente), ['PRUEBA01']);
  const sinTope = await req('/graphql', cliente, 'POST', { query });assert.equal(sinTope.data.data.vehiculosDisponibles.length, 2);
});
test('reserva por patente del formulario, cancelación y aparición en historial', async () => {
  const base = '/api_rest/v1/reservas';
  const r = await req(base, cliente, 'POST', { patenteVehiculo: 'PRUEBA01', fechaHoraInicio: '2035-02-01T10:00:00', fechaHoraFinal: '2035-02-03T10:00:00' });
  assert.equal(r.status, 201);
  assert.equal((await req(`${base}/${r.data.idReserva}`, cliente, 'PUT')).status, 200);
  const h = await req('/graphql', cliente, 'POST', { query: '{ miHistorialAlquileres { patente estado fechaInicio } }' });
  assert.ok(h.data.data.miHistorialAlquileres.some(r => r.fechaInicio === '2035-02-01T10:00:00' && r.estado === 'CANCELADO'));
});
test('admin puede crear reserva mediante DNI del cliente', async () => {
  const r = await req('/api_rest/v1/reservas', admin, 'POST', { dniCliente: '87654321', patenteVehiculo: 'PRUEBA02', fechaHoraInicio: '2035-03-01T10:00:00', fechaHoraFinal: '2035-03-03T10:00:00' });
  assert.equal(r.status, 201);
  const r2 = await req('/graphql', await login('cliente_dos'), 'POST', { query: '{ reservas { id } }' });
  assert.ok(r2.data.data.reservas.some(x => x.id === r.data.idReserva));
});
