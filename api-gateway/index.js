const express = require('express');
const grpc = require('@grpc/grpc-js');
const protoLoader = require('@grpc/proto-loader');
const path = require('path');

const app = express();
app.use(express.json());

// 1. Indicar la ruta de tus archivos .proto (asumiendo que la carpeta proto está un nivel arriba)
const PROTO_PATH = path.join(__dirname, '../proto/vehiculos/vehiculo.proto');

// 2. Cargar el contrato
const packageDefinition = protoLoader.loadSync(PROTO_PATH, {
    keepCase: true, longs: String, enums: String, defaults: true, oneofs: true
});
const vehiculosProto = grpc.loadPackageDefinition(packageDefinition).vehiculos;

// 3. Instanciar el cliente gRPC apuntando al puerto donde corre tu servicio-vehiculos (ej. 9090)
const vehiculoStub = new vehiculosProto.VehiculoService(
    'localhost:9090',
    grpc.credentials.createInsecure()
);