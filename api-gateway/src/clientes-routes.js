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
  if (req.query.dni) {
    const r = await llamarRpc(clientes, 'consultarClienteDni', { dni: String(req.query.dni) });
    return res.json([publico(clienteDeRespuesta(r))]);
  }
  const r = await llamarRpc(clientes, 'listarClientes', {});
  const lista = clienteDeRespuesta(r);
  if (!Array.isArray(lista)) return res.status(502).json({ error: 'Lista de clientes inválida' });
  res.json(lista.map(publico));
});

router.post('/', objetoJSON, async (req, res) => {
  res.status(201).json(publico(clienteDeRespuesta(await llamarRpc(clientes, 'crearCliente', datos(req.body)))));
});

// Rutas secundarias por DNI (declaradas antes de /:id para evitar colisión de parámetros)
router.get('/dni/:dni', async (req, res) => {
  const r = await llamarRpc(clientes, 'consultarClienteDni', { dni: req.params.dni });
  res.json(publico(clienteDeRespuesta(r)));
});

const actualizarPorDni = async (req, res) => {
  const r = await llamarRpc(clientes, 'modificarClienteDni', {
    ...datos(req.body),
    dni: req.params.dni || req.body.dni
  });
  res.json(publico(clienteDeRespuesta(r)));
};
router.patch('/dni/:dni', objetoJSON, actualizarPorDni);
router.put('/dni/:dni', objetoJSON, actualizarPorDni);

router.delete('/dni/:dni', async (req, res) => {
  const { id } = clienteDeRespuesta(
      await llamarRpc(clientes, 'consultarClienteDni', { dni: req.params.dni })
  );
  clienteDeRespuesta(await llamarRpc(clientes, 'borrarClienteId', { id }));
  res.send('Cliente dado de baja correctamente');
});

// Rutas principales por ID (/api_rest/v1/clientes/:id)
router.get('/:id', async (req, res) => {
  const r = await llamarRpc(clientes, 'consultarClienteId', { id: req.params.id });
  res.json(publico(clienteDeRespuesta(r)));
});

const actualizarPorId = async (req, res) => {
  const r = await llamarRpc(clientes, 'modificarClienteId', { ...datos(req.body), id: req.params.id });
  res.json(publico(clienteDeRespuesta(r)));
};
router.patch('/:id', objetoJSON, actualizarPorId);
router.put('/:id', objetoJSON, actualizarPorId);

router.delete('/:id', async (req, res) => {
  clienteDeRespuesta(await llamarRpc(clientes, 'borrarClienteId', { id: req.params.id }));
  res.send('Cliente dado de baja correctamente');
});

module.exports = router;