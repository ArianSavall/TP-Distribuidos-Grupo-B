const grpc = require('@grpc/grpc-js');

function exigirRol(...roles) {
  return (req, res, next) => {
    if (!req.session?.usuario) return res.status(401).json({ error: 'Debés iniciar sesión' });
    if (!roles.includes(req.session.usuario.rol)) return res.status(403).json({ error: 'Acceso denegado' });
    next();
  };
}
function objetoJSON(req, res, next) {
  if (!req.body || typeof req.body !== 'object' || Array.isArray(req.body)) {
    return res.status(400).json({ error: 'Se requiere un objeto JSON' });
  }
  next();
}
function noImplementado(mensaje) {
  return Object.assign(new Error(mensaje), { code: grpc.status.UNIMPLEMENTED });
}
function clienteDeRespuesta(r) {
  if (r.status && r.status !== 'SUCCESS') {
    throw Object.assign(new Error('El servicio de clientes rechazó la operación'), { code: grpc.status.INVALID_ARGUMENT });
  }
  if (!r.cliente) throw Object.assign(new Error('Respuesta de clientes incompleta'), { httpStatus: 502 });
  return r.cliente;
}
function campos(body, nombres) {
  return Object.fromEntries(nombres.filter(n => body[n] !== undefined).map(n => [n, body[n]]));
}
module.exports = { exigirRol, objetoJSON, noImplementado, clienteDeRespuesta, campos };
