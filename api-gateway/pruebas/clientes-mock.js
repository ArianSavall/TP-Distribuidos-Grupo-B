const path = require("node:path");
const grpc = require("@grpc/grpc-js");
const protoLoader = require("@grpc/proto-loader");

const definicion = protoLoader.loadSync(
  path.resolve(__dirname, "../../proto/contratos/clientes/clientes.proto"),
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

// Datos ficticios guardados únicamente en memoria.
const clientes = [
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
];

let siguienteId = 2;

function devolverCliente(cliente, responder) {
  if (!cliente) {
    responder({
      code: grpc.status.NOT_FOUND,
      details: "Cliente de prueba no encontrado"
    });
    return;
  }

  responder(null, {
    status: "SUCCESS",
    message: "Operación de prueba exitosa",
    cliente
  });
}

function darDeBaja(cliente, responder) {
  if (!cliente) {
    devolverCliente(cliente, responder);
    return;
  }

  cliente.estaActivo = false;
  devolverCliente(cliente, responder);
}

servidor.addService(contrato.clientes.ClienteService.service, {
  validarLogin: (llamada, responder) => {
    console.log("gRPC: validarLogin");

    const { usuario, password } = llamada.request.metadatos;

    // Credenciales ficticias, exclusivamente para esta prueba.
    if (usuario !== "cliente_prueba" || password !== "clave-prueba") {
      responder({
        code: grpc.status.UNAUTHENTICATED,
        details: "Usuario o contraseña incorrectos"
      });
      return;
    }

    const cliente = clientes.find(
      c => c.metadatos.usuario === usuario
    );

    if (!cliente || !cliente.estaActivo) {
      responder({
        code: grpc.status.PERMISSION_DENIED,
        details: "Usuario no habilitado"
      });
      return;
    }

    responder(null, {
      id: cliente.metadatos.id,
      usuario: cliente.metadatos.usuario,
      rol: cliente.metadatos.rol,
      nombre: cliente.nombre,
      apellido: cliente.apellido
    });
  },
  listarUsuarios: (llamada, responder) => responder(null, { cliente: clientes }),
  listarClientes: (llamada, responder) => {
    console.log("gRPC: listarClientes");

    responder(null, {
      status: "SUCCESS",
      message: "Listado de prueba",
      cliente: clientes
    });
  },

  consultarClienteId: (llamada, responder) => {
    console.log("gRPC: consultarClienteId");

    devolverCliente(
      clientes.find(c => c.id === llamada.request.id),
      responder
    );
  },

  consultarClienteDni: (llamada, responder) => {
    console.log("gRPC: consultarClienteDni");

    devolverCliente(
      clientes.find(c => c.dni === llamada.request.dni),
      responder
    );
  },

  crearCliente: (llamada, responder) => {
    console.log("gRPC: crearCliente");
    const datos = llamada.request;

    if (clientes.some(c => c.dni === datos.dni)) {
      responder({
        code: grpc.status.ALREADY_EXISTS,
        details: "DNI repetido en el servicio de prueba"
      });
      return;
    }

    const id = String(siguienteId++);

    const nuevo = {
      id,
      dni: datos.dni,
      nombre: datos.nombre,
      apellido: datos.apellido,
      email: datos.email,
      telefono: datos.telefono,
      fechaNacimiento: datos.fechaNacimiento,
      estaActivo: true,
      metadatos: {
        id,
        usuario: datos.metadatos?.usuario || "",
        password: "DATO_FICTICIO_NO_DEBE_SALIR",
        rol: "CLIENTE"
      }
    };

    clientes.push(nuevo);
    devolverCliente(nuevo, responder);
  },

  borrarClienteId: (llamada, responder) => {
    console.log("gRPC: borrarClienteId");

    darDeBaja(
      clientes.find(c => c.id === llamada.request.id),
      responder
    );
  },

  borrarClienteDni: (llamada, responder) => {
    console.log("gRPC: borrarClienteDni");

    darDeBaja(
      clientes.find(c => c.dni === llamada.request.dni),
      responder
    );
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
    console.log("Datos en memoria: se pierden al reiniciar.");
  }
);

process.on("SIGINT", () => {
  servidor.forceShutdown();
  process.exit(0);
});