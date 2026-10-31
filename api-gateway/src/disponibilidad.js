const { GraphQLError } = require('graphql');
const { vehiculos, llamarRpc } = require('./grpc-clients');
const { respuestaVehiculosParaFrontend, vehiculoParaGrpc } = require('./vehiculo-enums');

// El contrato date_time tiene precisión de minutos. No descartar segundos silenciosamente.
function fechaProto(valor) {
  const m = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::00(?:\.0+)?)?$/.exec(valor || '');
  if (!m) throw new GraphQLError('Usá fecha y hora sin zona horaria, con precisión de minutos', { extensions: { code: 'BAD_USER_INPUT' } });
  
  const fechaIso = `${m[1]}-${m[2]}-${m[3]}T${m[4]}:${m[5]}:00`;
  const fecha = new Date(`${fechaIso}Z`);

  if (!Number.isFinite(fecha.getTime()) || fecha.toISOString().slice(0, 16) !== valor.slice(0, 16)) {
    throw new GraphQLError('La fecha indicada no es válida', { extensions: { code: 'BAD_USER_INPUT' } });
  }
  return fechaIso;
}
async function consultarDisponibilidad(args) {
  const solicitud = {
    fechaDesde: fechaProto(args.fechaInicio), fechaHasta: fechaProto(args.fechaFinal),
    marca: args.marca || '', modelo: args.modelo || '',     tipoVehiculo: vehiculoParaGrpc({ tipoVehiculo: args.tipo || '' }).tipoVehiculo,
    precioDiarioMin: args.precioMin ?? 0,
    // Protobuf no distingue ausencia del cero en estos campos. Límite abierto explícito.
    precioDiarioMax: args.precioMax ?? Number.MAX_VALUE
  };
  try {
    const r = await llamarRpc(vehiculos, 'getVehiculosDisponibles', solicitud);
    if (!Array.isArray(r.vehiculos) || r.vehiculos.some(v => !v.patente || !v.tipoVehiculo)) {
      throw new Error('Respuesta de disponibilidad incompleta');
    }
    return respuestaVehiculosParaFrontend(r.vehiculos);
  } catch (e) {
    const code = e.code === 3 ? 'BAD_USER_INPUT' : e.code === 12 ? 'NOT_IMPLEMENTED' : 'SERVICIO_VEHICULOS_ERROR';
    throw new GraphQLError('No se pudo consultar la disponibilidad de vehículos', { extensions: { code } });
  }
}
module.exports = { consultarDisponibilidad };
