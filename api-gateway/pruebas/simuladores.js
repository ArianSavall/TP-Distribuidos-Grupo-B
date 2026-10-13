// Simuladores para probar el Gateway. No sustituyen las reglas ni las pruebas de Java.
const path = require('node:path');
const grpc = require('@grpc/grpc-js');
const loader = require('@grpc/proto-loader');

async function iniciarSimuladores() {
  const servidores = [];
  const credenciales = new Map([['cliente_prueba', 'clave-prueba'], ['cliente_dos', 'clave-prueba'], ['admin_prueba', 'clave-prueba']]);
  const clientes = [
    { id: '1', dni: '12345678', nombre: 'Cliente', apellido: 'De Prueba', email: 'cliente@example.com', telefono: '1100000000', fechaNacimiento: '2000-01-01', estaActivo: true, metadatos: { id: '1', usuario: 'cliente_prueba', rol: 'CLIENTE' } },
    { id: '2', dni: '87654321', nombre: 'Otro', apellido: 'Cliente', email: 'otro@example.com', telefono: '1100000001', fechaNacimiento: '2000-01-01', estaActivo: true, metadatos: { id: '2', usuario: 'cliente_dos', rol: 'CLIENTE' } },
    { id: '3', dni: '99999999', nombre: 'Admin', apellido: 'De Prueba', email: 'admin@example.com', telefono: '', fechaNacimiento: '1990-01-01', estaActivo: true, metadatos: { id: '3', usuario: 'admin_prueba', rol: 'ADMIN' } }
  ];
  const vehiculos = [
    { id: '1', marca: 'Fiat', modelo: 'Cronos', patente: 'PRUEBA01', tipoVehiculo: 'SEDAN', precioDiario: 25000, estaActivo: true, estado: 'DISPONIBLE', anio: 2024, color: 'Blanco' },
    { id: '2', marca: 'Toyota', modelo: 'Corolla', patente: 'PRUEBA02', tipoVehiculo: 'SEDAN', precioDiario: 35000, estaActivo: true, estado: 'DISPONIBLE', anio: 2023, color: 'Gris' }
  ];
  const fecha = dias => new Date(Date.now() + dias * 86400000).toISOString().slice(0, 10) + 'T10:00:00';
  const reservas = [
    { idReserva: '90', idCliente: '1', patenteVehiculo: 'PRUEBA01', fechaHoraInicio: fecha(-10), fechaHoraFinal: fecha(-8), estadoReserva: 'CONFIRMADO', importeTotal: 50000 },
    { idReserva: '100', idCliente: '1', patenteVehiculo: 'PRUEBA01', fechaHoraInicio: fecha(10), fechaHoraFinal: fecha(12), estadoReserva: 'CONFIRMADO', importeTotal: 50000 },
    { idReserva: '101', idCliente: '2', patenteVehiculo: 'PRUEBA02', fechaHoraInicio: fecha(10), fechaHoraFinal: fecha(12), estadoReserva: 'CONFIRMADO', importeTotal: 70000 }
  ];
  const error = (code, details) => Object.assign(new Error(details), { code, details });
  const existe = (dato, nombre) => { if (!dato) throw error(grpc.status.NOT_FOUND, `${nombre} no encontrado`); return dato; };
  const cliente = id => existe(clientes.find(c => c.id === id), 'Cliente');
  const porDni = dni => existe(clientes.find(c => c.dni === dni), 'Cliente');
  const vehiculo = patente => existe(vehiculos.find(v => v.patente === patente), 'Vehículo');
  const reserva = id => existe(reservas.find(r => r.idReserva === id), 'Reserva');
  const envolver = fn => (call, cb) => {
    try { cb(null, fn(call.request)); } catch (e) { cb(e.code == null ? error(grpc.status.INTERNAL, 'Error del simulador') : e); }
  };
  async function registrar(archivo, paquete, servicio, metodos) {
    const proto = grpc.loadPackageDefinition(loader.loadSync(path.resolve(__dirname, '../../proto/contratos', archivo), {
      keepCase: false, longs: String, enums: String, defaults: true, oneofs: true
    }));
    const s = new grpc.Server();
    s.addService(proto[paquete][servicio].service, Object.fromEntries(Object.entries(metodos).map(([k, fn]) => [k, envolver(fn)])));
    servidores.push(s);
    const puerto = await new Promise((resolve, reject) => s.bindAsync('127.0.0.1:0', grpc.ServerCredentials.createInsecure(), (e, p) => e ? reject(e) : resolve(p)));
    return `127.0.0.1:${puerto}`;
  }
  function modificar(c, d) {
    for (const key of ['dni','nombre','apellido','email','telefono','fechaNacimiento']) if (d[key]) c[key] = d[key];
    if (d.metadatos?.usuario) c.metadatos.usuario = d.metadatos.usuario;
    if (d.metadatos?.password) credenciales.set(c.metadatos.usuario, d.metadatos.password);
    return { cliente: c };
  }
  try {
    const CLIENTES_GRPC_ADDR = await registrar('clientes/clientes.proto', 'clientes', 'ClienteService', {
      validarLogin: d => {
        const c = clientes.find(c => c.metadatos.usuario === d.usuario);
        if (!c || credenciales.get(d.usuario) !== d.password) throw error(grpc.status.UNAUTHENTICATED, 'Credenciales incorrectas');
        if (!c.estaActivo) throw error(grpc.status.PERMISSION_DENIED, 'Usuario inactivo');
        return { ...c.metadatos, password: "hash-ficticio-no-exponer" };
      },
      listarUsuarios: () => ({ cliente: clientes }),
      listarClientes: () => ({ cliente: clientes.filter(c => c.metadatos.rol === 'CLIENTE') }),
      consultarClienteId: d => ({ cliente: cliente(d.id) }),
      consultarClienteDni: d => ({ cliente: porDni(d.dni) }),
      crearCliente: d => {
        if (!d.dni || !d.nombre || !d.metadatos?.usuario || !d.metadatos?.password) throw error(grpc.status.INVALID_ARGUMENT, 'Datos incompletos');
        if (clientes.some(c => c.dni === d.dni || c.metadatos.usuario === d.metadatos.usuario)) throw error(grpc.status.ALREADY_EXISTS, 'Cliente repetido');
        const id = String(clientes.length + 1);
        const c = { ...d, id, estaActivo: true, metadatos: { id, usuario: d.metadatos.usuario, rol: 'CLIENTE' } };
        clientes.push(c);credenciales.set(d.metadatos.usuario, d.metadatos.password);return { cliente: c };
      },
      modificarClienteId: d => modificar(cliente(d.id), d),
      modificarClienteDni: d => modificar(porDni(d.dni), d),
      borrarClienteId: d => { const c = cliente(d.id);c.estaActivo = false;return { cliente: c }; },
      borrarClienteDni: d => { const c = porDni(d.dni);c.estaActivo = false;return { cliente: c }; }
    });
    const date = d => `${d.year}-${String(d.month).padStart(2,'0')}-${String(d.day).padStart(2,'0')}T${String(d.hours).padStart(2,'0')}:${String(d.minutes).padStart(2,'0')}:00`;
    const VEHICULOS_GRPC_ADDR = await registrar('vehiculos/vehiculo.proto', 'vehiculos', 'VehiculoService', {
      getAllVehiculos: () => ({ vehiculos }),
      getVehiculoById: d => existe(vehiculos.find(v => v.id === d.id), 'Vehículo'),
      getVehiculoByPatente: d => vehiculo(d.patente),
      createVehiculo: d => {
        if (vehiculos.some(v => v.patente === d.patente)) throw error(grpc.status.ALREADY_EXISTS, 'Patente repetida');
        if (!d.patente || !d.marca || !d.modelo) throw error(grpc.status.INVALID_ARGUMENT, 'Datos incompletos');
        const v = { ...d, id: String(vehiculos.length + 1), estaActivo: true, estado: 'DISPONIBLE' };vehiculos.push(v);return v;
      },
      deleteVehiculo: d => { const v = vehiculo(d.patente);v.estaActivo = false;return v; },
      // No se inventan actualización/activación que el contrato real no permite identificar.
      getVehiculosDisponibles: d => {
        const desde = date(d.fechaDesde), hasta = date(d.fechaHasta);
        if (hasta <= desde) throw error(grpc.status.INVALID_ARGUMENT, 'Período inválido');
        return { vehiculos: vehiculos.filter(v => v.estaActivo &&
          (!d.marca || v.marca === d.marca) && (!d.modelo || v.modelo === d.modelo) &&
          (!d.tipoVehiculo || v.tipoVehiculo === d.tipoVehiculo) && v.precioDiario >= d.precioDiarioMin && v.precioDiario <= d.precioDiarioMax &&
          !reservas.some(r => r.patenteVehiculo === v.patente && r.estadoReserva === 'CONFIRMADO' && r.fechaHoraInicio < hasta && r.fechaHoraFinal > desde)) };
      }
    });
    const RESERVAS_GRPC_ADDR = await registrar('reservas/reserva.proto', 'reservas', 'ReservaService', {
      listarReservas: () => ({ reservas }),
      obtenerReserva: d => reserva(d.id),
      crearReserva: d => {
        if (!cliente(d.idCliente).estaActivo || !vehiculo(d.patenteVehiculo).estaActivo) throw error(grpc.status.FAILED_PRECONDITION, 'Cliente o vehículo inactivo');
        if (!d.fechaHoraInicio || d.fechaHoraFinal <= d.fechaHoraInicio) throw error(grpc.status.INVALID_ARGUMENT, 'Período inválido');
        const r = { ...d, idReserva: String(102 + reservas.length), estadoReserva: 'CONFIRMADO', importeTotal: 50000 };
        reservas.push(r);return r;
      },
      cancelarReserva: d => {
        const r = reserva(d.id);
        if (r.estadoReserva === 'CANCELADO') throw error(grpc.status.FAILED_PRECONDITION, 'Ya cancelada');
        r.estadoReserva = 'CANCELADO';return { exito: true, estadoReserva: 'CANCELADO' };
      },
      obtenerMiHistorial: d => ({ historial: reservas.filter(r => r.idCliente === d.idCliente &&
        (r.estadoReserva === 'CANCELADO' || r.fechaHoraFinal < new Date().toISOString().slice(0,19)))
        .sort((a,b) => b.fechaHoraInicio.localeCompare(a.fechaHoraInicio)).map(r => ({
          vehiculo: `${vehiculo(r.patenteVehiculo).marca} ${vehiculo(r.patenteVehiculo).modelo}`,
          patente: r.patenteVehiculo, fechaHoraInicio: r.fechaHoraInicio, fechaHoraFinal: r.fechaHoraFinal,
          dias: 2, importeTotal: r.importeTotal, estado: r.estadoReserva === 'CANCELADO' ? 'CANCELADO' : 'FINALIZADO'
        })) })
    });
    return { direcciones: { CLIENTES_GRPC_ADDR, VEHICULOS_GRPC_ADDR, RESERVAS_GRPC_ADDR }, cerrar: () => servidores.forEach(s => s.forceShutdown()) };
  } catch (e) { servidores.forEach(s => s.forceShutdown()); throw e; }
}
module.exports = { iniciarSimuladores };
