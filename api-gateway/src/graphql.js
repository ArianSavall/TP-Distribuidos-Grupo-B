const { buildSchema, GraphQLError } = require("graphql");
const { createHandler } = require("graphql-http/lib/use/express");
const { vehiculos, llamarRpc } = require("./grpc-clients");
const { consultarMiHistorial } = require("./historial");
const { consultarReservasCompletas } = require("./consulta-reservas");

const { consultarDisponibilidad } = require("./disponibilidad");
const { respuestaVehiculosParaFrontend } = require("./vehiculo-enums");

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

  enum TipoVehiculo { SEDAN SUV PICKUP COUPE HATCHBACK }

  input FiltroReservasInput {
    dniCliente: String
    patenteVehiculo: String
    tipoVehiculo: TipoVehiculo
    estado: EstadoReserva
    fechaDesde: String
    fechaHasta: String
  }

  enum EstadoReserva {
    CONFIRMADO
    CANCELADO
  }

  type Reserva {
    id: ID!
    cliente: String!
    vehiculo: String!
    patente: String!
    fechaInicio: String!
    fechaFinalizacion: String!
    precioDiario: Float!
    importeTotal: Float!
    estado: EstadoReserva!
    estadoMantenimiento: String!
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

  type VehiculoDisponible {
    id: ID!
    patente: String!
    marca: String!
    modelo: String!
    anio: Int!
    color: String
    tipoVehiculo: TipoVehiculo!
    precioDiario: Float!
  }

  type Query {
    vehiculosDisponibles(tipo: TipoVehiculo, marca: String, modelo: String,
      precioMin: Float, precioMax: Float, fechaInicio: String!, fechaFinal: String!): [VehiculoDisponible!]!

    vehiculos: [Vehiculo!]!
    reservas(filtros: FiltroReservasInput): [Reserva!]!
    miHistorialAlquileres: [HistorialAlquiler!]!
  }
`);

const rootValue = {
  vehiculosDisponibles: consultarDisponibilidad,
  vehiculos: async () => {
    try {
      const respuesta = await llamarRpc(
        vehiculos,
        "getAllVehiculos",
        {}
      );

      return respuestaVehiculosParaFrontend(respuesta.vehiculos);
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

  reservas: (argumentos, contexto) => {
    return consultarReservasCompletas(contexto.usuario, argumentos.filtros);
  },

  miHistorialAlquileres: (argumentos, contexto) => {
    return consultarMiHistorial(contexto.usuario);
  }
};

module.exports = createHandler({
  schema,
  rootValue,
  context: request => ({
    usuario: request.raw.session?.usuario || null
  })
});
