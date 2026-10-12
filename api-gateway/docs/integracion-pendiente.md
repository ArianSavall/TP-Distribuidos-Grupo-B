# Estado de integración del Gateway

Revisión del 12/10/2026. Base de Gateway: f1bb5fd + integración de develop en nuestra rama. Servicios revisados:
`origin/develop` 335d386 y `origin/Refactor/migracion-BD-cliente` a1bd715.
No se modifican los servicios Java en esta corrección.

## Verificado automáticamente

Desde `api-gateway`: `npm ci` y `npm test`.
Las 16 pruebas automáticas levantan servidores gRPC de prueba en puertos libres y el Gateway HTTP
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

- **Clientes:** falta implementar `AutenticarCliente` en Java. El contrato de esta
  rama ya define usuario/password y una respuesta de identidad (idCliente, usuario,
  rol, nombre, apellido). La contraseña debe verificarse dentro de Clientes con su
  almacenamiento actual; el Gateway sólo transporta las credenciales y establece
  la sesión. No se debe devolver la contraseña. La implementación publicada de
  Clientes utiliza BCrypt: no reemplazar contraseñas guardadas por texto plano.
- **Clientes:** la rama de migración a BD aún no está integrada en develop; cambia
  respuestas a `{ cliente }` sin status. La consulta de reservas ya admite ambos
  formatos; revisar también las rutas REST al integrar. En esa rama no está
  implementado `borrarClienteDni`. Los métodos de modificación comparan getters
  protobuf con null, aunque los campos omitidos devuelven valores predeterminados;
  corregir presencia de campos antes de probar modificaciones parciales.
- **Vehículos:** `PatchVehiculoRequest` no identifica el vehículo, y `saveVehiculo`
  crea una entidad nueva. Se necesita un contrato e implementación de actualización
  por patente/ID y una operación para reactivar vehículos.
- **Vehículos:** `getVehiculosDisponibles` envía fechas vacías a Reservas, no aplica
  aún todos los filtros y devuelve sólo ID/marca. Debe devolver el vehículo completo.
  Las consultas por patente rechazan vehículos inactivos: acordar cómo recuperar
  datos de vehículos dados de baja para reservas históricas.
- **Reservas:** alta conserva un precio fijo de prueba y validaciones entre servicios
  marcadas TODO. El RPC `GetVehiculosOcupados` está en el contrato pero sin implementar
  en el servicio revisado. El historial devuelve un nombre de vehículo provisional.
- **Puertos:** los mocks usan 9091/9092/9093; los YAML publicados de Vehículos y Reservas
  usan 9090 y 9091 respectivamente. Confirmar puerto de Clientes y configurar `.env`
  del Gateway para los servicios reales. No publicar credenciales de base de datos.

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
  Los originales permanecen en `resources/`. La interfaz llama solamente al Gateway.
- Arranque de prueba con `npm run demo`: un proceso, puertos gRPC libres y datos
  ficticios en memoria. No usa la base real. Pruebas con `npm test`.

## Para cerrar la entrega integrada

1. Acordar/implementar los pendientes de los servicios detallados arriba, incluyendo
   identificación de vehículo para actualización, reactivación y campos de modificación
   de cliente. Revisar las rutas del Gateway tras cualquier cambio de contrato.
2. Integrar el servicio Clientes y su RPC de autenticación sin perder el contrato
   agregado en esta rama. Verificar identidad ADMIN y CLIENTE proveniente de la BD.
3. Configurar puertos y levantar Java/MySQL. Ejecutar las siete funcionalidades con
   datos reales, incluyendo un vehículo inactivo, clientes diferentes y rechazo de
   reservas superpuestas. Los tests del Gateway no prueban esas reglas de Java.
4. Guardar capturas de integración y completar el informe grupal de arquitectura,
   modelo de datos e integrantes. Preparar PR hacia develop para revisión; no hacer merge
   automático ni declarar finalizada la entrega sólo con simuladores.
