const path = require("node:path");
const grpc = require("@grpc/grpc-js");
const protoLoader = require("@grpc/proto-loader");

const definicion = protoLoader.loadSync(
  path.resolve(__dirname, "../../proto/reservas/reserva.proto"),
  {
    keepCase: false,
    longs: String,
    enums: String,
    defaults: true,
    oneofs: true
  }
);

const contrato = grpc.loadPackageDefinition(definicion);
const servidor = new grpc.Server();

servidor.addService(contrato.reservas.ReservaService.service, {
  listarReservas: (llamada, responder) => {
    console.log("Consulta recibida por gRPC: listarReservas");

    responder(null, {
      reservas: [
        {
          idReserva: "100",
          patenteVehiculo: "PRUEBA01",
          idCliente: "1",
          fechaHoraInicio: "2026-11-10T10:00:00",
          fechaHoraFinal: "2026-11-12T10:00:00",
          estadoReserva: "CONFIRMADO",
          importeTotal: 50000
        }
      ]
    });
  }
});

servidor.bindAsync(
  "127.0.0.1:9093",
  grpc.ServerCredentials.createInsecure(),
  (error) => {
    if (error) {
      console.error(error.message);
      process.exitCode = 1;
      return;
    }

    console.log("Servicio gRPC de PRUEBA de reservas en 127.0.0.1:9093");
    console.log("Devuelve una reserva ficticia. No utiliza la base de datos.");
  }
);

process.on("SIGINT", () => {
  servidor.forceShutdown();
  process.exit(0);
});