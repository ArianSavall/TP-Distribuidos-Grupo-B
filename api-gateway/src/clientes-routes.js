const express = require("express");
const { clientes, llamarRpc } = require("./grpc-clients");

const router = express.Router();

// Excluye la contraseña de las respuestas al navegador.
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

// El contrato actual devuelve SUCCESS o ERROR.
// Todavía no distingue tipos de error de negocio.
function respuestaExitosa(respuesta, res) {
  if (respuesta.status === "SUCCESS") {
    return true;
  }

  if (respuesta.status === "ERROR") {
    res.status(400).json({
      error: "El servicio de clientes rechazó la operación"
    });
    return false;
  }

  res.status(502).json({
    error: "Respuesta inesperada del servicio de clientes"
  });
  return false;
}

// Selecciona los campos que admite el contrato de alta.
// El rol y el estado activo los determina el servicio.
function datosDeAlta(body) {
  return {
    dni: body.dni,
    nombre: body.nombre,
    apellido: body.apellido,
    email: body.email,
    telefono: body.telefono,
    fechaNacimiento: body.fechaNacimiento,
    metadatos: {
      usuario: body.metadatos?.usuario,
      password: body.metadatos?.password
    }
  };
}

// Listar clientes.
router.get("/", async (req, res, next) => {
  try {
    const respuesta = await llamarRpc(clientes, "listarClientes", {});

    if (!respuestaExitosa(respuesta, res)) return;

    res.json(respuesta.cliente.map(clientePublico));
  } catch (error) {
    next(error);
  }
});

// Consultar un cliente por ID.
router.get("/buscar/id/:id", async (req, res, next) => {
  try {
    const respuesta = await llamarRpc(clientes, "consultarClienteId", {
      id: req.params.id
    });

    if (!respuestaExitosa(respuesta, res)) return;

    if (!respuesta.cliente) {
      return res.status(502).json({
        error: "El servicio informó éxito pero no devolvió el cliente"
      });
    }

    res.json(clientePublico(respuesta.cliente));
  } catch (error) {
    next(error);
  }
});

// Consultar un cliente por DNI.
router.get("/buscar/dni/:dni", async (req, res, next) => {
  try {
    const respuesta = await llamarRpc(clientes, "consultarClienteDni", {
      dni: req.params.dni
    });

    if (!respuestaExitosa(respuesta, res)) return;

    if (!respuesta.cliente) {
      return res.status(502).json({
        error: "El servicio informó éxito pero no devolvió el cliente"
      });
    }

    res.json(clientePublico(respuesta.cliente));
  } catch (error) {
    next(error);
  }
});

// Alta de cliente.
router.post("/", async (req, res, next) => {
  try {
    if (!req.body || typeof req.body !== "object" || Array.isArray(req.body)) {
      return res.status(400).json({
        error: "Se requiere un objeto JSON"
      });
    }

    const respuesta = await llamarRpc(
      clientes,
      "crearCliente",
      datosDeAlta(req.body)
    );

    if (!respuestaExitosa(respuesta, res)) return;

    if (!respuesta.cliente) {
      return res.status(502).json({
        error: "El servicio informó éxito pero no devolvió el cliente creado"
      });
    }

    // Conserva el código HTTP 200 del controlador original.
    res.status(200).json(clientePublico(respuesta.cliente));
  } catch (error) {
    next(error);
  }
});

// Baja lógica por ID.
router.delete("/borrar/id/:id", async (req, res, next) => {
  try {
    const respuesta = await llamarRpc(clientes, "borrarClienteId", {
      id: req.params.id
    });

    if (!respuestaExitosa(respuesta, res)) return;

    res.status(200).send("Cliente dado de baja correctamente");
  } catch (error) {
    next(error);
  }
});

// Baja lógica por DNI.
router.delete("/borrar/dni/:dni", async (req, res, next) => {
  try {
    const respuesta = await llamarRpc(clientes, "borrarClienteDni", {
      dni: req.params.dni
    });

    if (!respuestaExitosa(respuesta, res)) return;

    res.status(200).send("Cliente dado de baja correctamente");
  } catch (error) {
    next(error);
  }
});

module.exports = router;