const path = require("node:path");
const grpc = require("@grpc/grpc-js");
const protoLoader = require("@grpc/proto-loader");

const definicion = protoLoader.loadSync(
  path.resolve(__dirname, "../../proto/clientes/clientes.proto"),
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

servidor.addService(contrato.clientes.ClienteService.service, {
  listarClientes: (llamada, responder) => {
    console.log("Consulta recibida por gRPC: listarClientes");

    responder(null, {
      status: "SUCCESS",
      message: "Clientes ficticios para prueba",
      cliente: [
        {
          id: "1",
          dni: "12345678",
          nombre: "Cliente",
          apellido: "De Prueba",
          email: "cliente@example.com",
          telefono: "1100000000",
          fechaNacimiento: "2000-01-01",
          estaActivo: true,
          metadatos: {
            id: "1",
            usuario: "cliente_prueba",
            password: "DATO_FICTICIO_NO_DEBE_SALIR",
            rol: "CLIENTE"
          }
        }
      ]
    });
  }
});

servidor.bindAsync(
  "127.0.0.1:9092",
  grpc.ServerCredentials.createInsecure(),
  (error) => {
    if (error) {
      console.error(error.message);
      process.exitCode = 1;
      return;
    }

    console.log("Servicio gRPC de PRUEBA de clientes en 127.0.0.1:9092");
  }
);

process.on("SIGINT", () => {
  servidor.forceShutdown();
  process.exit(0);
});