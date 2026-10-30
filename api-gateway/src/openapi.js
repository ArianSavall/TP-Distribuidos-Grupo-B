const openapi = {
  openapi: "3.0.3",
  info: {
    title: "Rentar API Gateway",
    version: "1.0.0",
    description: "API REST del gateway para clientes, vehículos y reservas."
  },
  servers: [{ url: "http://localhost:3000" }],
  tags: [
    { name: "Clientes" },
    { name: "Vehículos" },
    { name: "Reservas" },
    { name: "Sistema" }
  ],
  components: {
    securitySchemes: {
      sessionCookie: {
        type: "apiKey",
        in: "cookie",
        name: "connect.sid",
        description: "Cookie de sesión obtenida mediante POST /auth/login."
      }
    },
    schemas: {
      Error: {
        type: "object",
        properties: { error: { type: "string" } },
        required: ["error"]
      },
      Vehiculo: {
        type: "object",
        properties: {
          id: { type: "string" },
          patente: { type: "string", example: "AE123BB" },
          marca: { type: "string" },
          modelo: { type: "string" },
          anio: { type: "integer" },
          color: { type: "string" },
          tipoVehiculo: { type: "string", enum: ["SEDAN", "SUV", "PICKUP", "COUPE", "HATCHBACK"] },
          precioDiario: { type: "number", format: "double" },
          estaActivo: { type: "boolean" },
          estado: { type: "string", enum: ["DISPONIBLE", "RESERVADO", "EN_ALQUILER"] },
          resultadoVerificacion: { type: "string", enum: ["PENDIENTE", "VALIDADO", "ROBADO"] },
          habilitado: { type: "boolean" }
        }
      },
      VehiculoInput: {
        type: "object",
        required: ["marca", "modelo", "anio", "tipoVehiculo", "precioDiario"],
        properties: {
          marca: { type: "string" },
          modelo: { type: "string" },
          anio: { type: "integer" },
          color: { type: "string" },
          tipoVehiculo: { type: "string", enum: ["SEDAN", "SUV", "PICKUP", "COUPE", "HATCHBACK"] },
          precioDiario: { type: "number", format: "double" }
        }
      },
      ClienteInput: {
        type: "object",
        properties: {
          dni: { type: "string" },
          nombre: { type: "string" },
          apellido: { type: "string" },
          email: { type: "string", format: "email" },
          telefono: { type: "string" },
          fechaNacimiento: { type: "string", format: "date" },
          metadatos: {
            type: "object",
            properties: { usuario: { type: "string" }, password: { type: "string", format: "password" } }
          }
        }
      },
      ReservaInput: {
        type: "object",
        required: ["fechaHoraInicio", "fechaHoraFinal"],
        properties: {
          idCliente: { type: "string" },
          dniCliente: { type: "string" },
          idVehiculo: { type: "string" },
          patenteVehiculo: { type: "string" },
          fechaHoraInicio: { type: "string", format: "date-time" },
          fechaHoraFinal: { type: "string", format: "date-time" }
        }
      }
    }
  },
  paths: {
    "/health": {
      get: {
        tags: ["Sistema"],
        summary: "Estado del gateway",
        responses: { 200: { description: "Gateway disponible" } }
      }
    },
    "/api_rest/v1/clientes": {
      get: {
        tags: ["Clientes"],
        summary: "Lista clientes o busca por DNI",
        security: [{ sessionCookie: [] }],
        parameters: [{ name: "dni", in: "query", schema: { type: "string" } }],
        responses: { 200: { description: "Clientes" }, 401: { $ref: "#/components/responses/Unauthorized" } }
      },
      post: {
        tags: ["Clientes"],
        summary: "Crea un cliente",
        security: [{ sessionCookie: [] }],
        requestBody: { required: true, content: { "application/json": { schema: { $ref: "#/components/schemas/ClienteInput" } } } },
        responses: { 201: { description: "Cliente creado" }, 400: { $ref: "#/components/responses/BadRequest" } }
      }
    },
    "/api_rest/v1/clientes/{id}": {
      parameters: [{ $ref: "#/components/parameters/id" }],
      get: {
        tags: ["Clientes"], summary: "Obtiene un cliente", security: [{ sessionCookie: [] }],
        responses: { 200: { description: "Cliente" }, 404: { $ref: "#/components/responses/NotFound" } }
      },
      patch: {
        tags: ["Clientes"], summary: "Modifica un cliente", security: [{ sessionCookie: [] }],
        requestBody: { required: true, content: { "application/json": { schema: { $ref: "#/components/schemas/ClienteInput" } } } },
        responses: { 200: { description: "Cliente modificado" } }
      },
      delete: {
        tags: ["Clientes"], summary: "Da de baja un cliente", security: [{ sessionCookie: [] }],
        responses: { 200: { description: "Cliente dado de baja" } }
      }
    },
    "/api_rest/v1/vehiculos": {
      get: {
        tags: ["Vehículos"], summary: "Lista vehículos", security: [{ sessionCookie: [] }],
        responses: { 200: { description: "Vehículos" } }
      },
      post: {
        tags: ["Vehículos"], summary: "Crea un vehículo (ADMIN)", security: [{ sessionCookie: [] }],
        requestBody: { required: true, content: { "application/json": { schema: { allOf: [{ $ref: "#/components/schemas/VehiculoInput" }], properties: { patente: { type: "string" } } } } } },
        responses: { 201: { description: "Vehículo creado" } }
      }
    },
    "/api_rest/v1/vehiculos/{patente}": {
      parameters: [{ name: "patente", in: "path", required: true, schema: { type: "string" } }],
      get: {
        tags: ["Vehículos"], summary: "Obtiene un vehículo", security: [{ sessionCookie: [] }],
        responses: { 200: { description: "Vehículo", content: { "application/json": { schema: { $ref: "#/components/schemas/Vehiculo" } } } } }
      },
      patch: {
        tags: ["Vehículos"], summary: "Modifica un vehículo (ADMIN)", security: [{ sessionCookie: [] }],
        requestBody: { required: true, content: { "application/json": { schema: { $ref: "#/components/schemas/VehiculoInput" } } } },
        responses: { 200: { description: "Vehículo modificado" }, 501: { description: "Contrato gRPC no compatible" } }
      },
      delete: {
        tags: ["Vehículos"], summary: "Da de baja un vehículo (ADMIN)", security: [{ sessionCookie: [] }],
        responses: { 200: { description: "Vehículo dado de baja" } }
      }
    },
    "/api_rest/v1/vehiculos/externos": {
      post: {
        tags: ["Vehículos"], summary: "Registra un vehículo externo (CLIENTE)", security: [{ sessionCookie: [] }],
        requestBody: { required: true, content: { "application/json": { schema: { allOf: [{ $ref: "#/components/schemas/VehiculoInput" }], properties: { patente: { type: "string" } } } } } },
        responses: { 202: { description: "Registro iniciado" } }
      }
    },
    "/api_rest/v1/reservas": {
      post: {
        tags: ["Reservas"], summary: "Crea una reserva", security: [{ sessionCookie: [] }],
        requestBody: { required: true, content: { "application/json": { schema: { $ref: "#/components/schemas/ReservaInput" } } } },
        responses: { 201: { description: "Reserva creada" } }
      }
    },
    "/api_rest/v1/reservas/{id}": {
      parameters: [{ $ref: "#/components/parameters/id" }],
      delete: {
        tags: ["Reservas"], summary: "Cancela una reserva", security: [{ sessionCookie: [] }],
        responses: { 200: { description: "Reserva cancelada" }, 403: { $ref: "#/components/responses/Forbidden" } }
      },
      put: {
        tags: ["Reservas"], summary: "Cancela una reserva (compatibilidad)", security: [{ sessionCookie: [] }],
        responses: { 200: { description: "Reserva cancelada" } }
      }
    }
  }
};

openapi.components.parameters = {
  id: { name: "id", in: "path", required: true, schema: { type: "string", pattern: "^[1-9][0-9]*$" } }
};
openapi.components.responses = {
  BadRequest: { description: "Solicitud inválida", content: { "application/json": { schema: { $ref: "#/components/schemas/Error" } } } },
  Unauthorized: { description: "Autenticación requerida", content: { "application/json": { schema: { $ref: "#/components/schemas/Error" } } } },
  Forbidden: { description: "Acceso denegado", content: { "application/json": { schema: { $ref: "#/components/schemas/Error" } } } },
  NotFound: { description: "Recurso no encontrado", content: { "application/json": { schema: { $ref: "#/components/schemas/Error" } } } }
};

module.exports = openapi;
