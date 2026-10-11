const { instalarAutenticacion } = require("./auth");
const express = require("express");
const graphqlHandler = require("./graphql");
const clientesRoutes = require("./clientes-routes");
const vehiculosRoutes = require("./vehiculos-routes");
const grpc = require("@grpc/grpc-js");
const {
  vehiculos,
  llamarRpc,
  cerrarClientes
} = require("./grpc-clients");

const app = express();
const PORT = 3000;

app.use(express.json());
instalarAutenticacion(app);
app.all("/graphql", graphqlHandler);
app.use("/api_rest/v1/clientes", clientesRoutes);

app.get("/", (req, res) => {
  res.json({
    servicio: "API Gateway - Rentar",
    mensaje: "El Gateway está funcionando"
  });
});

// Comprueba el Gateway, no la disponibilidad de los servicios.
app.get("/health", (req, res) => {
  res.json({
    estado: "OK",
    servicio: "api-gateway"
  });
});

// Recibe la consulta REST y llama al servicio por gRPC.
app.get("/api_rest/v1/vehiculos/getAll", async (req, res, next) => {
  try {
    const respuesta = await llamarRpc(
      vehiculos,
      "getAllVehiculos",
      {}
    );

    res.json(respuesta.vehiculos);
  } catch (error) {
    next(error);
  }
});

app.use("/api_rest/v1/vehiculos", vehiculosRoutes);

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
    [grpc.status.UNIMPLEMENTED]: [501, "La operación todavía no está implementada en el servicio"]
  };

  const [estado, mensaje] = erroresGrpc[err.code] || [
    500,
    "Error interno del Gateway o del servicio"
  ];

  console.error(err.message);

  res.status(estado).json({
    error: mensaje
  });
});

const servidor = app.listen(PORT, "127.0.0.1", () => {
  console.log(`API Gateway de Rentar: http://localhost:${PORT}`);
});

// Cierra las conexiones al detener el Gateway.
process.on("SIGINT", () => {
  cerrarClientes();
  servidor.close(() => process.exit(0));
});