const express = require("express");
const { reservas, clientes, llamarRpc } = require("./grpc-clients");
const {
  crearReserva,
  cancelarReserva
} = require("./reservas-operaciones");

const router = express.Router();

function idValido(valor) {
  return /^[1-9]\d*$/.test(String(valor ?? ""));
}

router.use((req, res, next) => {
  const usuario = req.session?.usuario;

  if (!usuario) {
    return res.status(401).json({ error: "Debés iniciar sesión" });
  }

  if (!["CLIENTE", "ADMIN"].includes(usuario.rol)) {
    return res.status(403).json({ error: "Acceso denegado" });
  }

  next();
});

router.post("/", async (req, res, next) => {
  try {
    const usuario = req.session.usuario;
    const datos = req.body;

    if (!datos || typeof datos !== "object" || Array.isArray(datos)) {
      return res.status(400).json({ error: "Se requiere un objeto JSON" });
    }

    let idCliente;

    if (usuario.rol === "CLIENTE") {
      if (
        datos.idCliente != null &&
        String(datos.idCliente) !== String(usuario.id)
      ) {
        return res.status(403).json({
          error: "No podés crear reservas para otro cliente"
        });
      }

      idCliente = usuario.id;
    } else {
      idCliente = datos.idCliente;
      if (!idCliente && datos.dniCliente) {
        const { clienteDeRespuesta } = require("./http-utils");
        const r = await llamarRpc(clientes, "consultarClienteDni", { dni: datos.dniCliente });
        idCliente = clienteDeRespuesta(r).id;
      }
    }

    if (!idValido(idCliente) || (!datos.patenteVehiculo && !idValido(datos.idVehiculo))) {
      return res.status(400).json({
        error: "Los identificadores deben ser enteros positivos"
      });
    }

    const respuesta = await crearReserva({
      idCliente: String(idCliente),
      idVehiculo: datos.idVehiculo == null ? undefined : String(datos.idVehiculo),
      patenteVehiculo: datos.patenteVehiculo,
      fechaHoraInicio: datos.fechaHoraInicio,
      fechaHoraFinal: datos.fechaHoraFinal
    });

    res.status(201).json(respuesta);
  } catch (error) {
    next(error);
  }
});

router.put("/:id", async (req, res, next) => {
  try {
    if (!idValido(req.params.id)) {
      return res.status(400).json({
        error: "El identificador debe ser un entero positivo"
      });
    }

    const usuario = req.session.usuario;

    // Consulta la reserva antes de autorizar su cancelación.
    const reserva = await llamarRpc(
      reservas,
      "obtenerReserva",
      { id: req.params.id }
    );

    if (
      String(reserva.idReserva) !== req.params.id ||
      !idValido(reserva.idCliente)
    ) {
      return res.status(502).json({
        error: "El servicio devolvió una reserva incompleta o inesperada"
      });
    }

    if (
      usuario.rol === "CLIENTE" &&
      String(reserva.idCliente) !== String(usuario.id)
    ) {
      return res.status(403).json({
        error: "No podés cancelar reservas de otro cliente"
      });
    }

    const mensaje = await cancelarReserva(req.params.id);
    res.status(200).send(mensaje);
  } catch (error) {
    next(error);
  }
});

module.exports = router;