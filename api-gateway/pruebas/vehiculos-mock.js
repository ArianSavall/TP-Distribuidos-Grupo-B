const path = require("node:path");
const grpc = require("@grpc/grpc-js");
const protoLoader = require("@grpc/proto-loader");

// Usa el mismo contrato que el servicio Java.
const archivoProto = path.resolve(
  __dirname,
  "../../proto/vehiculos/vehiculo.proto"
);

const definicion = protoLoader.loadSync(archivoProto, {
  keepCase: false,
  longs: String,
  enums: String,
  defaults: true,
  oneofs: true
});

const contrato = grpc.loadPackageDefinition(definicion);
const servidor = new grpc.Server();

// Datos ficticios: no se consulta ninguna base de datos.
servidor.addService(contrato.vehiculos.VehiculoService.service, {
  getAllVehiculos: (llamada, responder) => {
    console.log("Consulta recibida por gRPC: getAllVehiculos");

    responder(null, {
      vehiculos: [
        {
          id: "1",
          marca: "Fiat",
          modelo: "Cronos",
          patente: "PRUEBA01",
          tipoVehiculo: "AUTO",
          precioDiario: 25000,
          estaActivo: true,
          estado: "DISPONIBLE",
          anio: 2024,
          color: "Blanco"
        },
        {
          id: "2",
          marca: "Toyota",
          modelo: "Corolla",
          patente: "PRUEBA02",
          tipoVehiculo: "AUTO",
          precioDiario: 35000,
          estaActivo: true,
          estado: "DISPONIBLE",
          anio: 2023,
          color: "Gris"
        }
      ]
    });
  }
});

servidor.bindAsync(
  "127.0.0.1:9091",
  grpc.ServerCredentials.createInsecure(),
  (error) => {
    if (error) {
      console.error("No se pudo iniciar el servicio de prueba:", error.message);
      process.exitCode = 1;
      return;
    }

    console.log("Servicio gRPC de PRUEBA en 127.0.0.1:9091");
    console.log("Devuelve datos ficticios. No utiliza la base de datos.");
  }
);

process.on("SIGINT", () => {
  servidor.forceShutdown();
  process.exit(0);
});