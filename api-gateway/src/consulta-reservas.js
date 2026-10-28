const { GraphQLError } = require("graphql");
const { reservas, clientes, vehiculos, llamarRpc } = require("./grpc-clients");
const { estadoReserva, estadoMantenimiento } = require("./enums");

function fallo(mensaje, code) {
  return new GraphQLError(mensaje, { extensions: { code } });
}

// Formato ISO local de Java: no interpreta las fechas según la zona del servidor.
function fechaLocal(valor) {
  if (!valor) return null;
  if (!/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(:\d{2}(\.\d{1,9})?)?$/.test(valor)) {
    throw fallo("Usá una fecha ISO sin zona horaria", "BAD_USER_INPUT");
  }
  const base = valor.slice(0, 19).padEnd(19, ":00");
  const fecha = new Date(base + "Z");
  if (!Number.isFinite(fecha.getTime()) || fecha.toISOString().slice(0, 19) !== base) {
    throw fallo("La fecha indicada no es válida", "BAD_USER_INPUT");
  }
  return base + "." + (valor.split(".")[1] || "").padEnd(9, "0");
}

// Combina datos de los tres servicios. Nunca consulta una base de datos.
async function consultarReservasCompletas(usuario, filtros = {}) {
  if (!usuario?.id) throw fallo("Debés iniciar sesión", "UNAUTHENTICATED");
  if (!["ADMIN", "CLIENTE"].includes(usuario.rol)) {
    throw fallo("No tenés permiso para consultar reservas", "FORBIDDEN");
  }
  filtros = filtros || {};
  const desde = fechaLocal(filtros.fechaDesde);
  const hasta = fechaLocal(filtros.fechaHasta);
  if (desde && hasta && desde > hasta) {
    throw fallo("fechaDesde no puede ser posterior a fechaHasta", "BAD_USER_INPUT");
  }

  try {
    const respuesta = await llamarRpc(reservas, "listarReservas", {});
    if (!Array.isArray(respuesta.reservas)) throw new Error("Lista de reservas inválida");
    // La identidad proviene exclusivamente de la sesión. Un filtro no amplía permisos.
    const permitidas = respuesta.reservas.filter(r =>
      (usuario.rol === "ADMIN" || String(r.idCliente) === String(usuario.id)) &&
      (!filtros.patenteVehiculo || r.patenteVehiculo === filtros.patenteVehiculo) &&
      (!filtros.estado || estadoReserva(r.estadoReserva) === filtros.estado) &&
      (!desde || fechaLocal(r.fechaHoraFinal) >= desde) &&
      (!hasta || fechaLocal(r.fechaHoraInicio) <= hasta)
    );
    if (!permitidas.length) return [];

    // Consulta únicamente las identidades y vehículos necesarios; reutiliza cada RPC.
    const clientesPorId = new Map();
    const vehiculosPorPatente = new Map();
    await Promise.all([
      ...[...new Set(permitidas.map(r => String(r.idCliente)))].map(async id => {
        const r = await llamarRpc(clientes, "consultarClienteId", { id });
        // Admite tanto el contrato anterior (status) como la respuesta nueva (cliente).
        if ((r.status && r.status !== "SUCCESS") || !r.cliente || String(r.cliente.id) !== id) {
          throw new Error("Respuesta de clientes inválida");
        }
        clientesPorId.set(id, r.cliente);
      }),
      ...[...new Set(permitidas.map(r => r.patenteVehiculo))].map(async patente => {
        const v = await llamarRpc(vehiculos, "getVehiculoByPatente", { patente });
        if (v.patente !== patente) throw new Error("Respuesta de vehículos inválida");
        vehiculosPorPatente.set(patente, v);
      })
    ]);

    return permitidas.filter(r => {
      const c = clientesPorId.get(String(r.idCliente));
      const v = vehiculosPorPatente.get(r.patenteVehiculo);
      return (usuario.rol !== "ADMIN" || !filtros.dniCliente || c.dni === filtros.dniCliente) &&
        (!filtros.tipoVehiculo || v.tipoVehiculo === filtros.tipoVehiculo);
    }).sort((a, b) => fechaLocal(b.fechaHoraInicio).localeCompare(fechaLocal(a.fechaHoraInicio)))
      .map(r => {
        const c = clientesPorId.get(String(r.idCliente));
        const v = vehiculosPorPatente.get(r.patenteVehiculo);
        return {
          id: r.idReserva,
          cliente: `${c.nombre} ${c.apellido}`.trim(),
          vehiculo: `${v.marca} ${v.modelo}`.trim(),
          patente: r.patenteVehiculo,
          fechaInicio: r.fechaHoraInicio,
          fechaFinalizacion: r.fechaHoraFinal,
          precioDiario: v.precioDiario,
          importeTotal: r.importeTotal,
          estado: estadoReserva(r.estadoReserva),
          estadoMantenimiento: estadoMantenimiento(r.estadoMantenimiento)
        };
      });
  } catch (error) {
    console.error("Error gRPC al consultar reservas:", error.message);
    throw fallo("No se pudo consultar las reservas", "SERVICIO_RESERVAS_ERROR");
  }
}

module.exports = { consultarReservasCompletas };
