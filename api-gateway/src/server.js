const reservasRoutes = require("./reservas-routes");
const { instalarAutenticacion } = require("./auth");
const express = require("express");
const swaggerUi = require("swagger-ui-express");
const openapi = require("./openapi");
const graphqlHandler = require("./graphql");
const clientesRoutes = require("./clientes-routes");
const vehiculosRoutes = require("./vehiculos-routes");
const grpc = require("@grpc/grpc-js");
const {
  cerrarClientes
} = require("./grpc-clients");

const app = express();
const PORT = Number(process.env.PORT || 3000);

app.use(express.json());
app.get("/api-docs/swagger.json", (req, res) => res.json(openapi));
app.use("/api-docs", swaggerUi.serve, swaggerUi.setup(openapi));
instalarAutenticacion(app);
app.all("/graphql", graphqlHandler);

app.use("/api_rest/v1/clientes", clientesRoutes);
app.use("/api_rest/v1/vehiculos", vehiculosRoutes);
app.use("/api_rest/v1/reservas", reservasRoutes);

require("./web")(app);

// Comprueba el Gateway, no la disponibilidad de los servicios.
app.get("/health", (req, res) => {
  res.json({
    estado: "OK",
    modo: process.env.RENTAR_DEMO === "1" ? "simulado" : "servicios",
    servicio: "api-gateway"
  });
});

app.use((req, res) => {
  res.status(404).json({
    error: "Ruta no encontrada"
  });
});

// Traduce errores gRPC a respuestas HTTP.
app.use((err, req, res, next) => {
  if (err.type === "entity.parse.failed") {
    return res.status(400).json({
      error: "El cuerpo de la solicitud no contiene un JSON válido"
    });
  }

  const erroresGrpc = {
    [grpc.status.INVALID_ARGUMENT]: [400, "Solicitud inválida"],
    [grpc.status.NOT_FOUND]: [404, "Recurso no encontrado"],
    [grpc.status.ALREADY_EXISTS]: [409, "El recurso ya existe"],
    [grpc.status.FAILED_PRECONDITION]: [409, "La operación no puede realizarse"],
    [grpc.status.UNAUTHENTICATED]: [401, "Autenticación requerida"],
    [grpc.status.PERMISSION_DENIED]: [403, "Acceso denegado"],
    [grpc.status.UNAVAILABLE]: [503, "El servicio interno no está disponible"],
    [grpc.status.DEADLINE_EXCEEDED]: [504, "El servicio interno tardó demasiado en responder"],
    [grpc.status.UNIMPLEMENTED]: [501, "Esta operación requiere completar el contrato o el servicio interno"],
    [grpc.status.CANCELLED]: [409, "El servicio rechazó la operación"]
  };

  const [estado, mensaje] = erroresGrpc[err.code] || [
    err.httpStatus || 500,
    "Error interno del Gateway o del servicio"
  ];
  const mensajeServicio =
      typeof err.details === "string" && err.details.trim()
          ? err.details
          : mensaje;

  console.error(mensajeServicio);

  res.status(estado).json({
    error: mensajeServicio
  });
});

// Permite probar el servidor HTTP en un puerto libre sin abrir el puerto habitual.
if (require.main === module) {
  const servidor = app.listen(PORT, "127.0.0.1", () => {
    console.log(`API Gateway de Rentar: http://localhost:${PORT}`);
  });
  process.on("SIGINT", () => {
    cerrarClientes();
    servidor.close(() => process.exit(0));
  });
}

module.exports = app;