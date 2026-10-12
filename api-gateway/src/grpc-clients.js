const path = require("node:path");
const grpc = require("@grpc/grpc-js");
const protoLoader = require("@grpc/proto-loader");

// Lee solamente el .env que pertenece al Gateway.
require("dotenv").config({
  path: path.resolve(__dirname, "../.env")
});

// Usa los contratos originales del grupo.
const PROTO_DIR = path.resolve(__dirname, "../../proto");

const opciones = {
  keepCase: false,
  longs: String,
  enums: String,
  defaults: true,
  oneofs: true
};

function crearCliente(archivo, paquete, servicio, direccion) {
  const definicion = protoLoader.loadSync(
    path.join(PROTO_DIR, archivo),
    opciones
  );

  const contrato = grpc.loadPackageDefinition(definicion);
  const Cliente = contrato[paquete][servicio];

  return new Cliente(
    direccion,
    grpc.credentials.createInsecure()
  );
}

// Direcciones locales provisionales.
// Deben coincidir con los puertos de los servicios al integrarlos.
const vehiculos = crearCliente(
  "vehiculos/vehiculo.proto",
  "vehiculos",
  "VehiculoService",
  process.env.VEHICULOS_GRPC_ADDR || "127.0.0.1:9091"
);

const clientes = crearCliente(
  "clientes/clientes.proto",
  "clientes",
  "ClienteService",
  process.env.CLIENTES_GRPC_ADDR || "127.0.0.1:9092"
);

const reservas = crearCliente(
  "reservas/reserva.proto",
  "reservas",
  "ReservaService",
  process.env.RESERVAS_GRPC_ADDR || "127.0.0.1:9093"
);

// Ejecuta una llamada gRPC con un límite de cinco segundos.
function llamarRpc(cliente, metodo, datos) {
  return new Promise((resolve, reject) => {
    if (typeof cliente[metodo] !== "function") {
      reject(Object.assign(new Error(`RPC no disponible: ${metodo}`), { code: grpc.status.UNIMPLEMENTED }));
      return;
    }
    cliente[metodo](
      datos,
      { deadline: new Date(Date.now() + 5000) },
      (error, respuesta) => {
        if (error) {
          reject(error);
          return;
        }

        resolve(respuesta);
      }
    );
  });
}

function cerrarClientes() {
  vehiculos.close();
  clientes.close();
  reservas.close();
}

module.exports = {
  vehiculos,
  clientes,
  reservas,
  llamarRpc,
  cerrarClientes
};