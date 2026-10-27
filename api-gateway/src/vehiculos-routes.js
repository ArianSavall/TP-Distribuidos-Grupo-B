const router = require('express').Router();
const { clientes, vehiculos, llamarRpc } = require('./grpc-clients');
const { exigirRol, objetoJSON, noImplementado, campos, clienteDeRespuesta } = require('./http-utils');
const datos = body => campos(body, ['marca', 'modelo', 'anio', 'color', 'tipoVehiculo', 'precioDiario']);



router.get('/getAll', async (req, res) => {
  res.json((await llamarRpc(vehiculos, 'getAllVehiculos', {})).vehiculos);
});
router.post('/create', exigirRol('ADMIN'), objetoJSON, async (req, res) => {
  res.json(await llamarRpc(vehiculos, 'createVehiculo', { ...datos(req.body), patente: req.body.patente }));
});
router.post('/desactivar', exigirRol('ADMIN'), objetoJSON, async (req, res) => {
  res.json(await llamarRpc(vehiculos, 'deleteVehiculo', { patente: req.body.patente }));
});
router.post('/activar', exigirRol('ADMIN'), objetoJSON, async (req, res) => {
  // Esta operación requiere acordar un RPC; no simular éxito ni crear otro vehículo.
  throw noImplementado('El contrato de vehículos todavía no ofrece reactivación');
});
router.post('/externos', exigirRol('CLIENTE'), objetoJSON, async (req, res) =>{
  const respuestaCliente = await llamarRpc(
    clientes,
    'consultarClienteId',
    { id: req.session.usuario.id}
  );
  const cliente = clienteDeRespuesta(respuestaCliente);

  const respuesta = await llamarRpc(vehiculos, 'registrarVehiculoExterno',{
    ...datos(req.body),
    patente: req.body.patente,
    clienteId: req.session.usuario.id,
    documento: cliente.dni
  });

  res.status(202).json(respuesta);
});
router.get('/:patente', async (req, res) => {
  res.json(await llamarRpc(vehiculos, 'getVehiculoByPatente', { patente: req.params.patente }));
});
router.put('/:patente', exigirRol('ADMIN'), objetoJSON, async (req, res) => {
  // No enviar una actualización si el contrato no puede identificar su destinatario.
  const fields = vehiculos.constructor.service.saveVehiculo?.requestType?.type?.field || [];
  if (!fields.some(f => f.name === 'patente')) {
    throw noImplementado('PatchVehiculoRequest necesita la patente para poder actualizar');
  }
  res.json(await llamarRpc(vehiculos, 'saveVehiculo', { ...datos(req.body), patente: req.params.patente }));
});
module.exports = router;
