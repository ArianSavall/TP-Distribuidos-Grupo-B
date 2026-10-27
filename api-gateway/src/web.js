const path = require('node:path');
const express = require('express');
const { exigirRol } = require('./http-utils');

module.exports = app => {
  const publicDir = path.resolve(__dirname, '../public');
  app.use('/assets', express.static(publicDir, { index: false, dotfiles: 'deny' }));
  app.get(['/login', '/auth/login'], (req, res) => {
    if (req.session?.usuario) return res.redirect('/');
    res.sendFile(path.join(publicDir, 'login.html'));
  });
  const sesion = (req, res, next) => req.session?.usuario ? next() : res.redirect('/login');
  const pagina = nombre => (req, res) => res.sendFile(path.join(publicDir, nombre + '.html'));
  app.get('/', sesion, pagina('index'));
  app.get('/clientes', sesion, exigirRol('ADMIN'), pagina('clientes'));
  app.get('/clientes/editar/:id', sesion, exigirRol('ADMIN'), pagina('clientesUpdate'));
  app.get('/vehiculos', sesion, exigirRol('ADMIN'), pagina('vehiculos'));
  app.get('/vehiculos/editar/:patente', sesion, exigirRol('ADMIN'), pagina('vehiculosUpdate'));
  app.get(['/reservas', '/disponibilidad', '/ui/disponibilidad'], sesion, pagina('reservas'));
  app.get('/historial', sesion, exigirRol('CLIENTE'), pagina('historial'));
  app.get('/vehiculos/externos/nuevo', sesion,exigirRol('CLIENTE'), pagina('vehiculoExternoNuevo'));
};
