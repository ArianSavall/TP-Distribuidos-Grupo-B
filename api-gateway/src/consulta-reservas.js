const {
  reservas,
  clientes,
  vehiculos,
  llamarRpc
} = require("./grpc-clients");

// Consulta los tres servicios y adapta sus respuestas.
// El acceso del usuario y los filtros se incorporarán
// antes de conectar esta función a una ruta pública.
async function consultarReservasCompletas() {
  const [respuestaReservas, respuestaClientes, respuestaVehiculos] =
    await Promise.all([
      llamarRpc(reservas, "listarReservas", {}),
      llamarRpc(clientes, "listarClientes", {}),
      llamarRpc(vehiculos, "getAllVehiculos", {})
    ]);

  if (respuestaClientes.status !== "SUCCESS") {
    throw new Error("El servicio de clientes no pudo completar la consulta");
  }

  const clientesPorId = new Map(
    respuestaClientes.cliente.map((cliente) => [
      String(cliente.id),
      cliente
    ])
  );

  const vehiculosPorPatente = new Map(
    respuestaVehiculos.vehiculos.map((vehiculo) => [
      vehiculo.patente,
      vehiculo
    ])
  );

  return respuestaReservas.reservas.map((reserva) => {
    const cliente = clientesPorId.get(String(reserva.idCliente));
    const vehiculo = vehiculosPorPatente.get(reserva.patenteVehiculo);

    if (!cliente || !vehiculo) {
      throw new Error(
        `No se pudieron completar los datos de la reserva ${reserva.idReserva}`
      );
    }

    return {
      id: reserva.idReserva,
      cliente: `${cliente.nombre} ${cliente.apellido}`.trim(),
      vehiculo: `${vehiculo.marca} ${vehiculo.modelo}`.trim(),
      patente: reserva.patenteVehiculo,
      fechaInicio: reserva.fechaHoraInicio,
      fechaFinalizacion: reserva.fechaHoraFinal,
      precioDiario: vehiculo.precioDiario,
      importeTotal: reserva.importeTotal,
      estado: reserva.estadoReserva
    };
  });
}

module.exports = { consultarReservasCompletas };