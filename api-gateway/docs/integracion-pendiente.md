# Estado de integración del Gateway

Revisión del 12/10/2026. Base de Gateway: 628db36 + integración de develop en nuestra rama. Servicios revisados:
`origin/develop` 73ed674, que integra Clientes y su validación de login (PR #29).
No se modifican los servicios Java en esta corrección.
Se adaptaron Gateway, simuladores y pruebas a los contratos movidos a `proto/contratos`.
Se usa el contrato de develop sin agregar `AutenticarCliente`: el RPC acordado es `validarLogin`.

## Verificado automáticamente

Desde `api-gateway`: `npm ci` y `npm test`.
Las 17 pruebas automáticas levantan servidores gRPC de prueba en puertos libres y el Gateway HTTP
real. No necesitan los mocks manuales, MySQL ni credenciales personales.
Prueban login rechazado/aceptado, sesión, consulta GraphQL de reservas y filtros,
aislación entre clientes, consulta administrativa, error de servicio, rechazo de
alta/cancelación ajena, publicación de historial y cierre de sesión.
**No equivalen a una prueba integrada con Java/MySQL.**

Se corrigió el archivo `consulta-reservas.js`, que contenía por error un segundo
handler GraphQL, y se publicó `reservas(filtros: FiltroReservasInput)` en el esquema
activo. La consulta une reservas, clientes y vehículos por gRPC. El cliente sólo
ve reservas de su ID de sesión; `dniCliente` únicamente aplica al administrador.
Las fechas filtran intersección inclusiva del período, como el repositorio del
Hito 1. Se mantienen los IDs como cadenas para no perder precisión de int64.

## Pendientes comprobados en los servicios publicados

- **Login implementado:** Clientes valida usuario/contraseña con BCrypt mediante
  `validarLogin`. El Gateway crea y destruye las sesiones. La respuesta contiene el
  ID de metadatos; el Gateway llama después a `ListarUsuarios` para encontrar el ID
  real del cliente, comprobando coincidencia única de metadatos, usuario y rol.
  Sólo guarda y devuelve ID de cliente, usuario, rol, nombre y apellido; nunca claves.
  Esta consulta adicional se puede reemplazar por un RPC específico o una respuesta
  de login que incluya idCliente cuando el grupo amplíe el contrato.
- **Clientes:** ya está integrado el almacenamiento en BD y las respuestas `{ cliente }`
  sin status. La baja por DNI del Gateway consulta el ID por gRPC y luego invoca
  `BorrarClienteId`, ya que el contrato no ofrece `BorrarClienteDni`.
  La modificación Java ahora omite cadenas vacías; no permite vaciar un campo existente.
  El servicio devuelve hashes de contraseña en metadatos: el Gateway los elimina
  de las respuestas REST, pero conviene quitarlos del mapeo Java.
- **Vehículos:** `PatchVehiculoRequest` no identifica el vehículo, y `saveVehiculo`
  crea una entidad nueva. Se necesita un contrato e implementación de actualización
  por patente/ID y una operación para reactivar vehículos.
- **Vehículos:** `getVehiculosDisponibles` ya envía fechas y consume patentes ocupadas,
  pero no aplica aún todos los filtros y devuelve sólo ID/marca. Debe devolver el vehículo completo.
  Las consultas por patente rechazan vehículos inactivos: acordar cómo recuperar
  datos de vehículos dados de baja para reservas históricas.
- **Reservas:** el alta ya consulta el precio real a Vehículos y `GetVehiculosOcupados`
  está implementado. Falta verificar cliente activo y superposición al crear reservas.
  El historial todavía devuelve un nombre de vehículo provisional.
- **Puertos reales:** Vehículos 9090, Clientes 9091, Reservas 9092 según los YAML
  publicados. Configurar el `.env` local; los mocks manuales mantienen 9091/9092/9093.
  `npm run demo` usa puertos libres y no necesita cambiar `.env`.

## Gateway implementado y límites de esta revisión

- REST de clientes: alta, lista, búsquedas, modificación y bajas; sólo ADMIN.
- REST de vehículos: lista, búsqueda, alta y baja. Actualizar por patente y activar
  tienen rutas, pero retornan HTTP 501 mientras el contrato no permite identificar
  el vehículo a actualizar o no ofrece un RPC de reactivación. No se presenta esto
  como funcionalidad terminada ni se simula éxito.
- REST de reservas: alta por ID o patente del formulario; ADMIN puede indicar DNI
  del cliente; CLIENTE usa su sesión. Cancelación con control de propietario.
- GraphQL: reservas con filtros, disponibilidad delegada a Vehículos, historial y
  vehículos. La disponibilidad falla explícitamente si recibe vehículos incompletos.
- Pantallas del Hito 1 adaptadas en `public/`, conservando diseño, formularios y CSS.
  La interfaz llama solamente al Gateway; develop eliminó el antiguo módulo monolítico.
- Arranque de prueba con `npm run demo`: un proceso, puertos gRPC libres y datos
  ficticios en memoria. No usa la base real. Pruebas con `npm test`.

## Para cerrar la entrega integrada

1. Acordar/implementar los pendientes de los servicios detallados arriba, incluyendo
   identificación de vehículo para actualización, reactivación y campos de modificación
   de cliente. Revisar las rutas del Gateway tras cualquier cambio de contrato.
2. Verificar login ADMIN y CLIENTE con credenciales de la BD real, clave incorrecta,
   usuario inactivo, ID de cliente correcto y cierre de sesión.
3. Configurar puertos y levantar Java/MySQL. Ejecutar las siete funcionalidades con
   datos reales, incluyendo un vehículo inactivo, clientes diferentes y rechazo de
   reservas superpuestas. Los tests del Gateway no prueban esas reglas de Java.
4. Guardar capturas de integración y completar el informe grupal de arquitectura,
   modelo de datos e integrantes. Preparar PR hacia develop para revisión; no hacer merge
   automático ni declarar finalizada la entrega sólo con simuladores.
