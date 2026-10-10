const express = require("express");
const { clientes, llamarRpc } = require("./grpc-clients");

const router = express.Router();

// Conserva los datos del cliente, pero excluye la contraseña.
function clientePublico(cliente) {
  const { metadatos, ...datos } = cliente;

  if (!metadatos) {
    return datos;
  }

  const { password, ...metadatosPublicos } = metadatos;

  return {
    ...datos,
    metadatos: metadatosPublicos
  };
}

router.get("/", async (req, res, next) => {
  try {
    const respuesta = await llamarRpc(
      clientes,
      "listarClientes",
      {}
    );

    // Este servicio también comunica errores dentro de su respuesta.
    if (respuesta.status !== "SUCCESS") {
      return res.status(502).json({
        error: "El servicio de clientes no pudo completar la consulta"
      });
    }

    res.json(respuesta.cliente.map(clientePublico));
  } catch (error) {
    next(error);
  }
});

module.exports = router;