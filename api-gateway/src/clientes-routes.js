const router = require('express').Router();
const { clientes, llamarRpc } = require('./grpc-clients');
const { exigirRol, objetoJSON, clienteDeRespuesta, campos } = require('./http-utils');
router.use(exigirRol('ADMIN'));

// Nunca devolver contraseñas, incluso si el servicio las incluye por error.
function publico(c) {
  const { metadatos, ...resto } = c;
  if (!metadatos) return resto;
  const { password, ...meta } = metadatos;
  return { ...resto, metadatos: meta };
}
function datos(body) {
  const r = campos(body, ['dni', 'nombre', 'apellido', 'email', 'telefono', 'fechaNacimiento']);
  if (body.metadatos && typeof body.metadatos === 'object') {
    r.metadatos = campos(body.metadatos, ['usuario', 'password']);
  }
  return r;
}
router.get('/', async (req, res) => {
  const r = await llamarRpc(clientes, 'listarClientes', {});
  const lista = clienteDeRespuesta(r);
  if (!Array.isArray(lista)) return res.status(502).json({ error: 'Lista de clientes inválida' });
  res.json(lista.map(publico));
});
for (const [clave, sufijo] of [['id', 'Id'], ['dni', 'Dni']]) {
  router.get(`/buscar/${clave}/:${clave}`, async (req, res) => {
    const r = await llamarRpc(clientes, `consultarCliente${sufijo}`, { [clave]: req.params[clave] });
    res.json(publico(clienteDeRespuesta(r)));
  });
  router.delete(`/borrar/${clave}/:${clave}`, async (req, res) => {
    clienteDeRespuesta(await llamarRpc(clientes, `borrarCliente${sufijo}`, { [clave]: req.params[clave] }));
    res.send('Cliente dado de baja correctamente');
  });
}
router.post('/', objetoJSON, async (req, res) => {
  res.json(publico(clienteDeRespuesta(await llamarRpc(clientes, 'crearCliente', datos(req.body)))));
});
router.patch('/modificar/id/:id', objetoJSON, async (req, res) => {
  const r = await llamarRpc(clientes, 'modificarClienteId', { ...datos(req.body), id: req.params.id });
  res.json(publico(clienteDeRespuesta(r)));
});
// Conserva el endpoint del controlador Java y permite también el DNI en la URL.
router.patch(['/modificar/dni', '/modificar/dni/:dni'], objetoJSON, async (req, res) => {
  const r = await llamarRpc(clientes, 'modificarClienteDni', {
    ...datos(req.body), dni: req.params.dni || req.body.dni
  });
  res.json(publico(clienteDeRespuesta(r)));
});
module.exports = router;
