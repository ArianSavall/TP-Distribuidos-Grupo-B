const { GraphQLError } = require("graphql");
const { reservas, llamarRpc } = require("./grpc-clients");

// "usuario" debe provenir de una autenticación verificada.
// No debe construirse con datos enviados libremente por el navegador.
async function consultarMiHistorial(usuario) {
  if (!usuario || !usuario.id) {
    throw new GraphQLError("Debés iniciar sesión", {
      extensions: { code: "UNAUTHENTICATED" }
    });
  }

  if (usuario.rol !== "CLIENTE") {
    throw new GraphQLError("El historial requiere el rol CLIENTE", {
      extensions: { code: "FORBIDDEN" }
    });
  }

  let respuesta;

  try {
    respuesta = await llamarRpc(
      reservas,
      "obtenerMiHistorial",
      { idCliente: String(usuario.id) }
    );
  } catch (error) {
    console.error("Error gRPC al consultar historial:", error.message);

    throw new GraphQLError("No se pudo consultar el historial", {
      extensions: { code: "SERVICIO_RESERVAS_ERROR" }
    });
  }

  // Adapta los nombres del contrato gRPC al GraphQL original.
  return respuesta.historial.map(item => ({
    vehiculo: item.vehiculo,
    patente: item.patente,
    fechaInicio: item.fechaHoraInicio,
    fechaFinalizacion: item.fechaHoraFinal,
    cantidadDias: item.dias,
    importeTotal: item.importeTotal,
    estado: item.estado
  }));
}

module.exports = { consultarMiHistorial };