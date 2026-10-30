const { buildSchema, GraphQLError } = require("graphql");
const { createHandler } = require("graphql-http/lib/use/express");
const { vehiculos, llamarRpc } = require("./grpc-clients");
const { consultarMiHistorial } = require("./historial");
const { consultarReservasCompletas } = require("./consulta-reservas");

const { consultarDisponibilidad } = require("./disponibilidad");
const { respuestaVehiculosParaFrontend } = require("./vehiculo-enums");

const schema = buildSchema(`
  """
  Tipos de vehículo admitidos por Rentar.
  """
  enum TipoVehiculo {
    "Vehículo tipo sedán."
    SEDAN
    "Utilitario deportivo."
    SUV
    "Camioneta con caja."
    PICKUP
    "Vehículo tipo coupé."
    COUPE
    "Vehículo tipo hatchback."
    HATCHBACK
  }

  """
  Estados almacenados de una reserva. FINALIZADO no existe acá: se deriva en
  el historial cuando una reserva confirmada ya terminó.
  """
  enum EstadoReserva {
    "Reserva vigente."
    CONFIRMADO
    "Reserva dada de baja de forma lógica; libera el período del vehículo."
    CANCELADO
  }

  """
  Vehículo de la flota, tal como lo devuelve el servicio de vehículos.
  """
  type Vehiculo {
    "Identificador del vehículo."
    id: ID!
    "Marca del vehículo."
    marca: String!
    "Modelo del vehículo."
    modelo: String!
    "Patente, única y no modificable."
    patente: String!
    "Tipo de vehículo: SEDAN, SUV, PICKUP, COUPE o HATCHBACK."
    tipoVehiculo: String!
    "Precio por día de alquiler."
    precioDiario: Float!
    "Falso si el vehículo fue dado de baja de forma lógica."
    estaActivo: Boolean!
    "Estado operativo del vehículo, por ejemplo DISPONIBLE o EN_ALQUILER."
    estado: String!
    "Año de fabricación."
    anio: Int!
    "Color del vehículo."
    color: String!
  }

  """
  Vehículo libre durante todo el período consultado.
  """
  type VehiculoDisponible {
    "Identificador del vehículo."
    id: ID!
    "Patente del vehículo."
    patente: String!
    "Marca del vehículo."
    marca: String!
    "Modelo del vehículo."
    modelo: String!
    "Año de fabricación."
    anio: Int!
    "Color del vehículo (puede no estar cargado)."
    color: String
    "Tipo de vehículo."
    tipoVehiculo: TipoVehiculo!
    "Precio por día de alquiler."
    precioDiario: Float!
  }

  """
  Filtros opcionales para buscar reservas. Se combinan con AND.
  Un filtro nunca amplía los permisos del usuario.
  """
  input FiltroReservasInput {
    "DNI del cliente. Solo lo aplica el ADMIN; para un CLIENTE se ignora."
    dniCliente: String
    "Patente exacta del vehículo."
    patenteVehiculo: String
    "Tipo del vehículo reservado."
    tipoVehiculo: TipoVehiculo
    "Estado de la reserva."
    estado: EstadoReserva
    "Inicio del rango. Trae reservas que terminan en esta fecha o después. Formato ISO local sin zona horaria: yyyy-MM-ddTHH:mm[:ss]."
    fechaDesde: String
    "Fin del rango. Trae reservas que empiezan en esta fecha o antes. Mismo formato que fechaDesde. No puede ser anterior a fechaDesde."
    fechaHasta: String
  }

  """
  Reserva con los datos del cliente y del vehículo ya combinados por el gateway.
  """
  type Reserva {
    "Identificador de la reserva."
    id: ID!
    "Nombre y apellido del cliente."
    cliente: String!
    "Marca y modelo del vehículo."
    vehiculo: String!
    "Patente del vehículo reservado."
    patente: String!
    "Inicio del alquiler, ISO local sin zona horaria."
    fechaInicio: String!
    "Fin del alquiler, ISO local sin zona horaria."
    fechaFinalizacion: String!
    "Precio por día del vehículo."
    precioDiario: Float!
    "Importe total: días de alquiler (mínimo 1) por precio diario."
    importeTotal: Float!
    "Estado de la reserva."
    estado: EstadoReserva!
    "Resultado del mantenimiento preventivo: PENDIENTE, REALIZADO o NO_REALIZADO. PENDIENTE significa que el resultado todavía no llegó por el broker de mensajes."
    estadoMantenimiento: String!
  }

  """
  Un alquiler del historial de un cliente.
  """
  type HistorialAlquiler {
    "Marca y modelo del vehículo."
    vehiculo: String!
    "Patente del vehículo."
    patente: String!
    "Inicio del alquiler, ISO local sin zona horaria."
    fechaInicio: String!
    "Fin del alquiler, ISO local sin zona horaria."
    fechaFinalizacion: String!
    "Cantidad de días del alquiler (mínimo 1)."
    cantidadDias: Int!
    "Importe total del alquiler."
    importeTotal: Float!
    "CANCELADO, o FINALIZADO si era una reserva confirmada cuyo período ya terminó."
    estado: String!
  }

  type Query {
    """
    Vehículos libres durante todo el período indicado: activos, habilitados
    y sin reservas confirmadas que se solapen con el rango. Los filtros de
    marca, modelo y tipo son exactos; el precio se filtra por rango.
    Errores: BAD_USER_INPUT (fecha inválida), SERVICIO_VEHICULOS_ERROR.
    """
    vehiculosDisponibles(
      "Tipo de vehículo (opcional)."
      tipo: TipoVehiculo
      "Marca exacta (opcional)."
      marca: String
      "Modelo exacto (opcional)."
      modelo: String
      "Precio diario mínimo (opcional)."
      precioMin: Float
      "Precio diario máximo (opcional). Si se omite no hay tope."
      precioMax: Float
      "Inicio del período. Obligatorio. Formato yyyy-MM-ddTHH:mm, con precisión de minutos y sin zona horaria."
      fechaInicio: String!
      "Fin del período. Obligatorio. Mismo formato que fechaInicio."
      fechaFinal: String!
    ): [VehiculoDisponible!]!

    """
    Vehículos activos de la flota.
    """
    vehiculos: [Vehiculo!]!

    """
    Reservas con cliente y vehículo ya resueltos, de la más reciente a la más
    antigua por fecha de inicio. Requiere sesión iniciada.
    ADMIN: ve las reservas de todos los clientes.
    CLIENTE: ve solamente las propias, sin importar los filtros.
    Errores: UNAUTHENTICATED, FORBIDDEN, BAD_USER_INPUT (fecha inválida o
    fechaDesde posterior a fechaHasta), SERVICIO_RESERVAS_ERROR.
    """
    reservas(
      "Filtros opcionales. Sin filtros devuelve todas las reservas permitidas."
      filtros: FiltroReservasInput
    ): [Reserva!]!

    """
    Historial del cliente autenticado: reservas canceladas y reservas
    confirmadas cuyo período ya terminó, de la más reciente a la más antigua.
    Solo para el rol CLIENTE; el cliente sale de la sesión, no de un parámetro.
    Errores: UNAUTHENTICATED, FORBIDDEN (para un ADMIN), SERVICIO_RESERVAS_ERROR.
    """
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
