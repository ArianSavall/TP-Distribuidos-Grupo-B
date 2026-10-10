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

// Datos ficticios: se reinician al cerrar este programa.
const reservas = [
  {
    idReserva: "100",
    patenteVehiculo: "PRUEBA01",
    idCliente: "1",
    fechaHoraInicio: "2026-11-10T10:00:00",
    fechaHoraFinal: "2026-11-12T10:00:00",
    estadoReserva: "CONFIRMADO",
    importeTotal: 50000
  }
];

let siguienteId = 101;

servidor.addService(contrato.reservas.ReservaService.service, {
  obtenerMiHistorial: (llamada, responder) => {
    console.log(
      "gRPC: obtenerMiHistorial para cliente",
      llamada.request.idCliente
    );

    // Caso ficticio para comprobar el contrato y el mapeo.
    // No implementa la selección real del historial.
    const historial = llamada.request.idCliente === "1"
      ? [
          {
            vehiculo: "Fiat Cronos",
            patente: "PRUEBA01",
            fechaHoraInicio: "2026-09-01T10:00:00",
            fechaHoraFinal: "2026-09-03T10:00:00",
            dias: 2,
            importeTotal: 50000,
            estado: "FINALIZADO"
          }
        ]
      : [];

    responder(null, { historial });
  },
  listarReservas: (llamada, responder) => {
    console.log("gRPC: listarReservas");
    responder(null, { reservas });
  },

  obtenerReserva: (llamada, responder) => {
    console.log("gRPC: obtenerReserva");

    const reserva = reservas.find(
      r => r.idReserva === llamada.request.id
    );

    if (!reserva) {
      responder({
        code: grpc.status.NOT_FOUND,
        details: "Reserva de prueba no encontrada"
      });
      return;
    }

    responder(null, reserva);
  },

  crearReserva: (llamada, responder) => {
    console.log("gRPC: crearReserva");
    const datos = llamada.request;

    // Comprueba que el Gateway haya obtenido y enviado la patente.
    if (datos.patenteVehiculo !== "PRUEBA01") {
      responder({
        code: grpc.status.INVALID_ARGUMENT,
        details: "Esta prueba requiere la patente PRUEBA01"
      });
      return;
    }

    const nueva = {
      idReserva: String(siguienteId++),
      patenteVehiculo: datos.patenteVehiculo,
      idCliente: datos.idCliente,
      fechaHoraInicio: datos.fechaHoraInicio,
      fechaHoraFinal: datos.fechaHoraFinal,
      estadoReserva: "CONFIRMADO",
      // Importe fijo de prueba, no un cálculo real de alquiler.
      importeTotal: 50000
    };

    reservas.push(nueva);
    responder(null, nueva);
  },

  cancelarReserva: (llamada, responder) => {
    console.log("gRPC: cancelarReserva");

    const reserva = reservas.find(
      r => r.idReserva === llamada.request.id
    );

    if (!reserva) {
      responder({
        code: grpc.status.NOT_FOUND,
        details: "Reserva de prueba no encontrada"
      });
      return;
    }

    if (reserva.estadoReserva === "CANCELADO") {
      responder({
        code: grpc.status.FAILED_PRECONDITION,
        details: "La reserva de prueba ya está cancelada"
      });
      return;
    }

    reserva.estadoReserva = "CANCELADO";

    responder(null, {
      exito: true,
      estadoReserva: reserva.estadoReserva
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
    console.log("Datos e importes ficticios, guardados en memoria.");
  }
);

process.on("SIGINT", () => {
  servidor.forceShutdown();
  process.exit(0);
});