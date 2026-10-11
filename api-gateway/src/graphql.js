const { buildSchema, GraphQLError } = require("graphql");
const { createHandler } = require("graphql-http/lib/use/express");
const { vehiculos, llamarRpc } = require("./grpc-clients");
const { consultarMiHistorial } = require("./historial");

const schema = buildSchema(`
  type Vehiculo {
    id: ID!
    marca: String!
    modelo: String!
    patente: String!
    tipoVehiculo: String!
    precioDiario: Float!
    estaActivo: Boolean!
    estado: String!
    anio: Int!
    color: String!
  }

  type HistorialAlquiler {
    vehiculo: String!
    patente: String!
    fechaInicio: String!
    fechaFinalizacion: String!
    cantidadDias: Int!
    importeTotal: Float!
    estado: String!
  }

  type Query {
    vehiculos: [Vehiculo!]!
    miHistorialAlquileres: [HistorialAlquiler!]!
  }
`);

const rootValue = {
  vehiculos: async () => {
    try {
      const respuesta = await llamarRpc(
        vehiculos,
        "getAllVehiculos",
        {}
      );

      return respuesta.vehiculos;
    } catch (error) {
      console.error("Error gRPC al consultar vehículos:", error.message);

      throw new GraphQLError(
        "No se pudo completar la consulta al servicio de vehículos",
        {
          extensions: {
            code: "SERVICIO_VEHICULOS_ERROR"
          }
        }
      );
    }
  },

  miHistorialAlquileres: async (argumentos, contexto) => {
    return consultarMiHistorial(contexto.usuario);
  }
};

module.exports = createHandler({
  schema,
  rootValue,
  context: (request) => ({
    usuario: request.raw.session?.usuario || null
  })
});