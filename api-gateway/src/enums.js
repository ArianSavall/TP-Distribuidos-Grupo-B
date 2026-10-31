// Traduce los enums del contrato gRPC (con prefijo, por Buf Lint) a los valores
// que siempre expusieron el REST y el GraphQL del gateway.
function sinPrefijo(valor, prefijo, porDefecto) {
  if (typeof valor !== "string") return porDefecto;
  if (!valor.startsWith(prefijo)) return valor;
  const resto = valor.slice(prefijo.length);
  return resto === "UNSPECIFIED" ? porDefecto : resto;
}

const estadoReserva = valor => sinPrefijo(valor, "ESTADO_RESERVA_", null);
const estadoMantenimiento = valor => sinPrefijo(valor, "ESTADO_MANTENIMIENTO_", "PENDIENTE");
const estadoHistorial = valor => sinPrefijo(valor, "ESTADO_HISTORIAL_", null);

module.exports = { estadoReserva, estadoMantenimiento, estadoHistorial };