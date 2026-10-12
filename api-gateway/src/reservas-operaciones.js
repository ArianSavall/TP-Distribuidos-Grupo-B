const {
  reservas,
  vehiculos,
  llamarRpc
} = require("./grpc-clients");

// Recibe los campos del formulario original.
// Consulta la patente antes de llamar al servicio de reservas.
async function crearReserva(datos) {
  const vehiculo = await llamarRpc(
    vehiculos,
    datos.patenteVehiculo ? "getVehiculoByPatente" : "getVehiculoById",
    datos.patenteVehiculo ? { patente: datos.patenteVehiculo } : { id: datos.idVehiculo }
  );

  if (!vehiculo.patente) {
    throw new Error("El servicio de vehículos no devolvió la patente");
  }

  const reserva = await llamarRpc(
    reservas,
    "crearReserva",
    {
      idCliente: datos.idCliente,
      patenteVehiculo: vehiculo.patente,
      fechaHoraInicio: datos.fechaHoraInicio,
      fechaHoraFinal: datos.fechaHoraFinal
    }
  );

  // Conserva los campos del DTO de respuesta original.
  return {
    idReserva: reserva.idReserva,
    fechaHoraInicio: reserva.fechaHoraInicio,
    fechaHoraFinal: reserva.fechaHoraFinal,
    importeTotal: reserva.importeTotal,
    estadoReserva: reserva.estadoReserva
  };
}

async function cancelarReserva(id) {
  const respuesta = await llamarRpc(
    reservas,
    "cancelarReserva",
    { id }
  );

  if (!respuesta.exito) {
    throw new Error("El servicio no confirmó la cancelación");
  }

  return "Reserva cancelada exitosamente.";
}

module.exports = {
  crearReserva,
  cancelarReserva
};