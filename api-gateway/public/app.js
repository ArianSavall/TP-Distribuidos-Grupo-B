'use strict';
// Todas las solicitudes van al mismo origen HTTP del Gateway, nunca a un servicio interno.
const page = document.body.dataset.page;
const statusEl = document.getElementById('estado-pagina');
let identidad;
function mensaje(texto, error = false) {
  statusEl.hidden = !texto;
  statusEl.textContent = texto;
  statusEl.classList.toggle('error', error);
}
async function api(ruta, method = 'GET', body) {
  const r = await fetch(ruta, {
    method, credentials: 'same-origin',
    headers: body === undefined ? {} : { 'Content-Type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body)
  });
  const data = r.headers.get('content-type')?.includes('json') ? await r.json() : await r.text();
  if (!r.ok) {
    if (r.status === 401 && page !== 'login') location.assign('/login');
    throw new Error(data.error || 'No se pudo completar la operación');
  }
  return data;
}
async function graphql(query, variables = {}) {
  const r = await api('/graphql', 'POST', { query, variables });
  if (r.errors?.length) throw new Error(r.errors[0].message);
  return r.data;
}
function formulario(form) {
  const datos = {};
  for (const [nombre, valor] of new FormData(form)) {
    if (nombre.startsWith('metadatos.')) {
      (datos.metadatos ||= {})[nombre.split('.')[1]] = valor;
    } else datos[nombre] = valor;
  }
  for (const campo of ['anio', 'precioDiario']) {
    if (campo in datos) datos[campo] = Number(datos[campo]);
  }
  return datos;
}
function rellenar(form, datos) {
  for (const el of form.elements) {
    if (!el.name || el.type === 'password') continue;
    const value = el.name.split('.').reduce((v, key) => v?.[key], datos);
    if (value !== undefined && value !== null) el.value = value;
  }
}
function boton(texto, accion, css = 'btn-secondary') {
  const b = document.createElement('button');b.type = 'button';b.className = `btn ${css} btn-sm`;b.textContent = texto;
  b.addEventListener('click', async () => {
    b.disabled = true;
    try { await accion(); } catch (e) { mensaje(e.message, true); } finally { b.disabled = false; }
  });
  return b;
}
function enlace(texto, href) {
  const a = document.createElement('a');a.textContent = texto;a.href = href;a.className = 'btn btn-primary btn-sm';return a;
}
function tabla(nombre, rows, columnas, acciones) {
  const body = document.querySelector(`[data-table="${nombre}"]`);
  body.replaceChildren();
  if (!rows.length) {
    const tr = body.insertRow(); const td = tr.insertCell();td.colSpan = columnas.length + Number(!!acciones);
    td.textContent = 'No se encontraron resultados.';return;
  }
  for (const row of rows) {
    const tr = body.insertRow();
    for (const valor of columnas) tr.insertCell().textContent = typeof valor === 'function' ? valor(row) : (row[valor] ?? '');
    if (acciones) { const td = tr.insertCell();for (const nodo of acciones(row)) td.append(nodo); }
  }
}
const dinero = n => Number(n).toLocaleString('es-AR', { style: 'currency', currency: 'ARS' });
async function cargarVehiculos(patente) {
  const base = '/api_rest/v1/vehiculos';
  const rows = patente ? [await api(`${base}/${encodeURIComponent(patente)}`)] : await api(`${base}/getAll`);
  tabla('vehiculos', rows, ['patente', v => `${v.marca} ${v.modelo}`, 'anio', 'tipoVehiculo', v => dinero(v.precioDiario), 'estado', v => v.estaActivo ? 'ACTIVO' : 'INACTIVO'], v => [
    enlace('Editar', `/vehiculos/editar/${encodeURIComponent(v.patente)}`),
    boton(v.estaActivo ? 'Baja' : 'Alta', async () => {
      if (!confirm(`¿${v.estaActivo ? 'Dar de baja' : 'Reactivar'} el vehículo ${v.patente}?`)) return;
      await api(`${base}/${v.estaActivo ? 'desactivar' : 'activar'}`, 'POST', { patente: v.patente });
      await cargarVehiculos();mensaje('Estado actualizado.');
    }, 'btn-danger')
  ]);
}
async function cargarClientes(dni) {
  const base = '/api_rest/v1/clientes';
  const rows = dni ? [await api(`${base}/buscar/dni/${encodeURIComponent(dni)}`)] : await api(base);
  tabla('clientes', rows, ['dni', c => `${c.nombre} ${c.apellido}`, 'email', 'telefono', c => c.metadatos?.usuario || '', c => c.estaActivo ? 'ACTIVO' : 'INACTIVO'], c => [
    enlace('Editar', `/clientes/editar/${encodeURIComponent(c.id)}`),
    ...(c.estaActivo ? [boton('Baja', async () => {
      if (!confirm(`¿Dar de baja a ${c.nombre} ${c.apellido}?`)) return;
      await api(`${base}/borrar/id/${encodeURIComponent(c.id)}`, 'DELETE');
      await cargarClientes();mensaje('Cliente dado de baja.');
    }, 'btn-danger')] : [])
  ]);
}
async function cargarReservas(f = {}) {
  const filtros = Object.fromEntries(Object.entries(f).filter(([,v]) => v !== ''));
  if (filtros.fechaDesde?.length === 10) filtros.fechaDesde += 'T00:00:00';
  if (filtros.fechaHasta?.length === 10) filtros.fechaHasta += 'T23:59:59';
  const r = await graphql(`query($f: FiltroReservasInput) { reservas(filtros: $f) {
    id cliente vehiculo patente fechaInicio fechaFinalizacion importeTotal estado
  } }`, { f: filtros });
  tabla('reservas', r.reservas, ['cliente', r => `${r.vehiculo} (${r.patente})`, 'fechaInicio', 'fechaFinalizacion', r => dinero(r.importeTotal), 'estado'], r =>
    r.estado === 'CANCELADO' ? [] : [boton('Cancelar', async () => {
      if (!confirm(`¿Cancelar la reserva ${r.id}?`)) return;
      await api(`/api_rest/v1/reservas/${encodeURIComponent(r.id)}`, 'PUT');
      await cargarReservas();mensaje('Reserva cancelada.');
    }, 'btn-danger')]
  );
}
async function disponibilidad(d) {
  const variables = { inicio: d.fechaInicioDisp, fin: d.fechaFinalDisp,
    tipo: d.tipoVehiculoDisp || null, marca: d.marcaDisp || null, modelo: d.modeloDisp || null,
    min: d.precioMinDisp === '' ? null : Number(d.precioMinDisp), max: d.precioMaxDisp === '' ? null : Number(d.precioMaxDisp) };
  const r = await graphql(`query($inicio:String!, $fin:String!, $tipo:TipoVehiculo, $marca:String, $modelo:String, $min:Float, $max:Float) {
    vehiculosDisponibles(fechaInicio:$inicio, fechaFinal:$fin, tipo:$tipo, marca:$marca, modelo:$modelo, precioMin:$min, precioMax:$max) {
      patente marca modelo tipoVehiculo color anio precioDiario
    }
  }`, variables);
  tabla('disponibilidad', r.vehiculosDisponibles, ['patente', v => `${v.marca} ${v.modelo}`, 'tipoVehiculo', 'color', 'anio', v => dinero(v.precioDiario)]);
  mensaje(`Vehículos disponibles: ${r.vehiculosDisponibles.length}.`);
}
async function historial() {
  const data = await graphql('{ miHistorialAlquileres { vehiculo patente fechaInicio fechaFinalizacion cantidadDias importeTotal estado } }');
  document.getElementById('tabla').hidden = false;
  tabla('historial', data.miHistorialAlquileres, ['vehiculo', 'patente', 'fechaInicio', 'fechaFinalizacion', 'cantidadDias', r => dinero(r.importeTotal), 'estado']);
  mensaje(`Alquileres encontrados: ${data.miHistorialAlquileres.length}.`);
}
async function accion(form) {
  const d = formulario(form);
  const id = encodeURIComponent(decodeURIComponent(location.pathname.split('/').pop()));
  switch (form.dataset.action) {
    case 'login': await api('/auth/login', 'POST', d); location.assign('/'); break;
    case 'logout': await api('/auth/logout', 'POST'); location.assign('/login'); break;
    case 'vehiculos-create': await api('/api_rest/v1/vehiculos/create', 'POST', d); form.reset(); await cargarVehiculos(); mensaje('Vehículo registrado.'); break;
    case 'vehiculos-search': await cargarVehiculos(d.patente); break;
    case 'vehiculosUpdate': await api(`/api_rest/v1/vehiculos/${id}`, 'PUT', d); location.assign('/vehiculos'); break;
    case 'clientes-create': await api('/api_rest/v1/clientes', 'POST', d); form.reset(); await cargarClientes(); mensaje('Cliente registrado.'); break;
    case 'clientes-search': await cargarClientes(d.dni); break;
    case 'clientesUpdate': await api(`/api_rest/v1/clientes/modificar/id/${id}`, 'PATCH', d); location.assign('/clientes'); break;
    case 'reservas-create':
      if (identidad.rol !== 'ADMIN') delete d.dniCliente;
      await api('/api_rest/v1/reservas', 'POST', d); form.reset(); await cargarReservas(); mensaje('Reserva registrada.'); break;
    case 'reservas-search': await cargarReservas(d); break;
    case 'disponibilidad': await disponibilidad(d); break;
  }
}
for (const form of document.querySelectorAll('form[data-action]')) {
  form.addEventListener('submit', async e => {
    e.preventDefault();const b = form.querySelector('button[type=submit]');if (b) b.disabled = true;
    mensaje('Procesando…');
    try { mensaje('');await accion(form); } catch (e) { mensaje(e.message, true); }
    finally { if (b) b.disabled = false; }
  });
}
document.querySelector('button[data-action=historial]')?.addEventListener('click', async e => {
  e.target.disabled = true;
  try { await historial(); } catch (e) { mensaje(e.message, true); } finally { e.target.disabled = false; }
});
(async () => {
  try {
  const salud = await api('/health');
  if (salud.modo === 'simulado') {
    const aviso = document.createElement('p');
    aviso.className = 'estado-pagina';
    aviso.textContent = 'Modo de prueba: datos ficticios que se borran al cerrar.';
    document.body.prepend(aviso);
  }
  if (page === 'login') return;
    identidad = (await api('/auth/me')).usuario;
    for (const el of document.querySelectorAll('[data-role]')) el.hidden = el.dataset.role !== identidad.rol;
    if (page === 'vehiculos') await cargarVehiculos();
    if (page === 'clientes') await cargarClientes();
    if (page === 'reservas') await cargarReservas();
    if (page === 'historial') await historial();
    const key = encodeURIComponent(decodeURIComponent(location.pathname.split('/').pop()));
    if (page === 'vehiculosUpdate') rellenar(document.querySelector('form'), await api(`/api_rest/v1/vehiculos/${key}`));
    if (page === 'clientesUpdate') rellenar(document.querySelector('form'), await api(`/api_rest/v1/clientes/buscar/id/${key}`));
  } catch (e) { mensaje(e.message, true); }
})();
