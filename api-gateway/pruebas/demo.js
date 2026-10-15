const { iniciarSimuladores } = require('./simuladores');
(async () => {
  const simuladores = await iniciarSimuladores();
  Object.assign(process.env, simuladores.direcciones, { RENTAR_DEMO: '1' });
  const app = require('../src/server');
  const { cerrarClientes } = require('../src/grpc-clients');
  const server = app.listen(Number(process.env.PORT || 3000), '127.0.0.1', () => {
    console.log(`Rentar de PRUEBA: http://localhost:${server.address().port}`);
    console.log('Usuarios: admin_prueba, cliente_prueba, cliente_dos. Clave: clave-prueba');
    console.log('Sólo datos ficticios en memoria. No utiliza servicios ni bases reales.');
  });
  function cerrar() { cerrarClientes();simuladores.cerrar();server.close(); }
  server.on('error', e => { console.error(e.code === 'EADDRINUSE' ? 'El puerto está ocupado. Cerrá el Gateway anterior con Ctrl+C.' : e.message);cerrar();process.exitCode = 1; });
  process.once('SIGINT', cerrar);process.once('SIGTERM', cerrar);
})().catch(e => { console.error(e.message);process.exitCode = 1; });
