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
    secret: randomBytes(32).toString("hex"),
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
      const identidad = await llamarRpc(
        clientes,
        "autenticarCliente",
        { usuario: usuario.trim(), password }
      );

      const id = String(identidad.idCliente);

      if (
        !/^[1-9]\d*$/.test(id) ||
        !["CLIENTE", "ADMIN"].includes(identidad.rol) ||
        !identidad.usuario
      ) {
        return res.status(502).json({
          error: "El servicio de clientes devolvió una identidad inválida"
        });
      }

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
        usuario: identidad.usuario,
        rol: identidad.rol,
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