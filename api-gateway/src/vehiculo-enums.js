const PREFIJOS = {
  tipoVehiculo: 'TIPO_VEHICULO_',
  estado: 'ESTADO_VEHICULO_',
  resultadoValidacionPatente: 'RESULTADO_VALIDACION_EXTERNA_',
  resultadoValidacionTitular: 'RESULTADO_VALIDACION_EXTERNA_',
  validacionTitularidad: 'RESULTADO_VALIDACION_EXTERNA_',
  resultadoVerificacion: 'RESULTADO_VERIFICACION_'
};

function conPrefijo(valor, prefijo) {
  if (!valor || valor === 'UNSPECIFIED' || valor.startsWith(prefijo)) return valor;
  return prefijo + valor;
}

function sinPrefijo(valor) {
  if (typeof valor !== 'string') return valor;
  return valor.replace(
    /^(TIPO_VEHICULO_|ESTADO_VEHICULO_|RESULTADO_VALIDACION_EXTERNA_|RESULTADO_VERIFICACION_)/,
    ''
  );
}

function vehiculoParaGrpc(vehiculo) {
  const resultado = { ...vehiculo };
  for (const [campo, prefijo] of Object.entries(PREFIJOS)) {
    if (resultado[campo] !== undefined) resultado[campo] = conPrefijo(resultado[campo], prefijo);
  }
  return resultado;
}

function vehiculoParaFrontend(vehiculo) {
  if (!vehiculo || typeof vehiculo !== 'object') return vehiculo;
  const resultado = { ...vehiculo };
  for (const campo of Object.keys(PREFIJOS)) {
    if (resultado[campo] !== undefined) resultado[campo] = sinPrefijo(resultado[campo]);
  }
  return resultado;
}

function respuestaVehiculosParaFrontend(respuesta) {
  return Array.isArray(respuesta)
    ? respuesta.map(vehiculoParaFrontend)
    : vehiculoParaFrontend(respuesta);
}

module.exports = {
  vehiculoParaGrpc,
  respuestaVehiculosParaFrontend
};
