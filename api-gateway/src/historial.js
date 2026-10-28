const { GraphQLError } = require("graphql");
const { reservas, vehiculos, llamarRpc } = require("./grpc-clients");
const { estadoHistorial } = require("./enums");

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

  // La marca y el modelo pertenecen al servicio de vehículos: el gateway los
  // completa consultando una sola vez cada patente.
  const patentes = [...new Set(respuesta.historial.map(item => item.patente))];
  const nombres = new Map();

  await Promise.all(patentes.map(async patente => {
    try {
      const v = await llamarRpc(vehiculos, "getVehiculoByPatente", { patente });
      if (v.patente === patente) {
        nombres.set(patente, `${v.marca} ${v.modelo}`.trim());
      }
    } catch (error) {
      // Si vehículos no responde para una patente, el historial igual se muestra.
      console.error(`No se pudo obtener el vehículo ${patente}:`, error.message);
    }
  }));

  // Adapta los nombres del contrato gRPC al GraphQL original.
  return respuesta.historial.map(item => ({
    vehiculo: nombres.get(item.patente) || item.vehiculo,
    patente: item.patente,
    fechaInicio: item.fechaHoraInicio,
    fechaFinalizacion: item.fechaHoraFinal,
    cantidadDias: item.dias,
    importeTotal: item.importeTotal,
    estado: estadoHistorial(item.estado)
  }));
}

module.exports = { consultarMiHistorial };