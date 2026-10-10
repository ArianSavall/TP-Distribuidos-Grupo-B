const { buildSchema, GraphQLError } = require("graphql");
const { createHandler } = require("graphql-http/lib/use/express");
const { vehiculos, llamarRpc } = require("./grpc-clients");

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

  type Query {
    vehiculos: [Vehiculo!]!
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
  }
};

module.exports = createHandler({
  schema,
  rootValue
});