const express = require("express");
const session = require("express-session");
const { randomBytes } = require("node:crypto");
const { clientes, llamarRpc } = require("./grpc-clients");

function instalarAutenticacion(app) {
  // Permite recibir formularios, además del JSON configurado en server.js.
  app.use(express.urlencoded({ extended: false }));

  // Sesiones en memoria para el desarrollo local del TP.
  // Al reiniciar el Gateway, habrá que iniciar sesión nuevamente.
  app.use(session({
    name: "rentar.sid",
    secret: process.env.SESSION_SECRET || randomBytes(32).toString("hex"),
    resave: false,
    saveUninitialized: false,
    cookie: {
      httpOnly: true,
      sameSite: "strict",
      secure: false,
      maxAge: 2 * 60 * 60 * 1000
    }
  }));

  app.post("/auth/login", async (req, res, next) => {
    try {
      const usuario = req.body?.usuario;
      const password = req.body?.password;

      if (
        typeof usuario !== "string" ||
        typeof password !== "string" ||
        !usuario.trim() ||
        !password
      ) {
        return res.status(400).json({
          error: "Ingresá usuario y contraseña"
        });
      }

      // La verificación se delega al servicio de clientes.
      const metadatos = await llamarRpc(
        clientes,
        "validarLogin",
        { usuario: usuario.trim(), password }
      );

      // validarLogin devuelve el ID de metadatos, no el ID del cliente.
      // Resolver la identidad con los RPC existentes, sólo tras validar la clave.
      if (!/^[1-9]\d*$/.test(String(metadatos.id)) ||
          metadatos.usuario !== usuario.trim() ||
          !["CLIENTE", "ADMIN"].includes(metadatos.rol)) {
        return res.status(502).json({ error: "El servicio de clientes devolvió una identidad inválida" });
      }
      const respuesta = await llamarRpc(clientes, "listarUsuarios", {});
      const coincidencias = Array.isArray(respuesta.cliente) ? respuesta.cliente.filter(c =>
        String(c.metadatos?.id) === String(metadatos.id) &&
        c.metadatos?.usuario === metadatos.usuario && c.metadatos?.rol === metadatos.rol
      ) : [];
      const identidad = coincidencias.length === 1 ? coincidencias[0] : null;
      if (!identidad || !/^[1-9]\d*$/.test(String(identidad.id))) {
        return res.status(502).json({ error: "No se pudo identificar al usuario autenticado" });
      }
      if (!identidad.estaActivo) {
        return res.status(403).json({ error: "Usuario inactivo" });
      }
      const id = String(identidad.id);

      // Genera una sesión nueva después de autenticar.
      await new Promise((resolve, reject) => {
        req.session.regenerate(error => {
          if (error) reject(error);
          else resolve();
        });
      });

      // Guarda identidad y rol. Nunca guarda la contraseña.
      req.session.usuario = {
        id,
        usuario: metadatos.usuario,
        rol: metadatos.rol,
        nombre: identidad.nombre,
        apellido: identidad.apellido
      };

      await new Promise((resolve, reject) => {
        req.session.save(error => {
          if (error) reject(error);
          else resolve();
        });
      });

      res.json({ usuario: req.session.usuario });
    } catch (error) {
      next(error);
    }
  });

  app.get("/auth/me", (req, res) => {
    if (!req.session.usuario) {
      return res.status(401).json({
        error: "Debés iniciar sesión"
      });
    }

    res.json({ usuario: req.session.usuario });
  });

  app.post("/auth/logout", (req, res, next) => {
    req.session.destroy(error => {
      if (error) {
        next(error);
        return;
      }

      res.clearCookie("rentar.sid", { path: "/" });
      res.json({ mensaje: "Sesión cerrada" });
    });
  });

  // Exige sesión para acceder a las API.
  app.use(
    ["/api_rest/v1", "/api/v1", "/graphql"],
    (req, res, next) => {
      if (!req.session.usuario) {
        return res.status(401).json({
          error: "Debés iniciar sesión"
        });
      }

      next();
    }
  );
}

module.exports = { instalarAutenticacion };