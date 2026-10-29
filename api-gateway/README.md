# API Gateway de Rentar

Node.js + Express recibe REST, GraphQL y la interfaz web. Los clientes gRPC cargan
los contratos del directorio `../proto/contratos`. El Gateway no abre conexiones a bases de
datos: la persistencia, precios, disponibilidad y validaciones de dominio pertenecen
a los servicios Java.

## Requisitos y arranque

Node.js 24 y npm. Ejecutar desde `api-gateway`:

```bat
npm ci
npm test
npm run demo
```

Abrir http://localhost:3000. La demostración levanta los tres servicios gRPC
simulados y la web en un solo proceso, sin MySQL ni otras ventanas. Usa puertos gRPC
libres, aunque exista un `.env` con direcciones reales. Cerrar con Ctrl+C.
Si el puerto 3000 está ocupado, detener el Gateway anterior o configurar `PORT`.

Credenciales **ficticias, sólo para la demostración**:

| Usuario | Clave | Rol |
|---|---|---|
| admin_prueba | clave-prueba | ADMIN |
| cliente_prueba | clave-prueba | CLIENTE |
| cliente_dos | clave-prueba | CLIENTE |

Los datos y sesiones se pierden al cerrar. Los importes y reglas de los simuladores
no validan el comportamiento de los servicios Java. Las rutas de edición y
reactivación de vehículos muestran un error mientras falten operaciones en el
contrato real; el simulador no inventa un contrato distinto.

## Servicios reales

1. Incorporar los cambios acordados del grupo a la rama propia.
2. Implementar los pendientes de [integración](docs/integracion-pendiente.md).
3. Si todavía no existe `.env`, copiar `.env.example` a `.env` y configurar
   `VEHICULOS_GRPC_ADDR`, `CLIENTES_GRPC_ADDR` y `RESERVAS_GRPC_ADDR` con los puertos
   **gRPC**, no HTTP, de los servicios. No sobrescribir un `.env` existente.
4. Levantar los servicios Java y sus bases siguiendo las instrucciones de sus autores.
5. Ejecutar `npm start` desde `api-gateway`. Este comando usa servicios reales y no
   arranca simuladores ni cambia automáticamente a ellos si una conexión falla.

Puertos publicados en develop `73ed674`: Vehículos `127.0.0.1:9090`, Clientes
`127.0.0.1:9091`, Reservas `127.0.0.1:9092`.

El login usa `validarLogin` de Clientes. Como devuelve el ID de metadatos, se consulta
`ListarUsuarios` tras validar las credenciales para obtener el ID del cliente correcto.
El Gateway mantiene la sesión y descarta las contraseñas/hashes recibidos.

`PORT` es opcional (3000 por defecto). `SESSION_SECRET` es opcional en desarrollo;
si no está definido se genera un secreto al arrancar. Las sesiones se guardan en
memoria con vigencia de dos horas. Reiniciar requiere volver a iniciar sesión.
La cookie es HttpOnly y SameSite=Strict. Esta configuración es para ejecución local
por HTTP; un despliegue externo requiere HTTPS, cookie Secure y almacenamiento de
sesiones adecuado. No guardar contraseñas en código ni en el historial de Git.

## Interfaz conservada

Las pantallas originales del Hito 1 están en `resources/templates` como referencia.
`public/` adapta su HTML/CSS a Node y reemplaza Thymeleaf por llamadas al Gateway:

- `/login` y `/`: inicio de sesión y panel.
- `/clientes` y `/clientes/editar/:id`: administración de clientes.
- `/vehiculos` y `/vehiculos/editar/:patente`: administración de vehículos.
- `/reservas`: disponibilidad, alta, filtros y cancelación.
- `/historial`: alquileres del cliente autenticado.

La autorización también se verifica en el servidor. Un CLIENTE no puede administrar
clientes o vehículos, indicar otro cliente para una reserva ni cancelar una reserva
ajena. ADMIN consulta reservas de todos y puede usar el DNI para crear una reserva.
El historial personal requiere CLIENTE.

## REST

La documentación interactiva OpenAPI/Swagger está disponible en
`http://localhost:3000/api-docs`. El documento JSON se puede consultar en
`http://localhost:3000/api-docs/swagger.json`.

Todas las API salvo login/health requieren la cookie de sesión.

| Método | Ruta | Uso |
|---|---|---|
| POST | /auth/login | JSON `{usuario,password}` → sesión e identidad |
| GET | /auth/me | Identidad autenticada |
| POST | /auth/logout | Invalida sesión y cookie |
| GET | /health | Estado del proceso, no de sus dependencias |
| GET/POST | /api_rest/v1/clientes | Listar/crear (ADMIN) |
| GET | /api_rest/v1/clientes/buscar/id/:id | Consultar por ID (ADMIN) |
| GET | /api_rest/v1/clientes/buscar/dni/:dni | Consultar por DNI (ADMIN) |
| PATCH | /api_rest/v1/clientes/modificar/id/:id | Modificar (ADMIN) |
| PATCH | /api_rest/v1/clientes/modificar/dni | Modificar con DNI en JSON (ADMIN) |
| DELETE | /api_rest/v1/clientes/borrar/id/:id | Baja por ID (ADMIN) |
| DELETE | /api_rest/v1/clientes/borrar/dni/:dni | Baja por DNI (ADMIN) |
| GET | /api_rest/v1/vehiculos/getAll | Listar vehículos |
| GET | /api_rest/v1/vehiculos/:patente | Consultar vehículo |
| POST | /api_rest/v1/vehiculos/create | Alta (ADMIN) |
| POST | /api_rest/v1/vehiculos/desactivar | Baja, JSON `{patente}` (ADMIN) |
| PATCH | /api_rest/v1/vehiculos/:patente | Pendiente de contrato identificable, 501 (ADMIN) |
| POST | /api_rest/v1/vehiculos/activar | Pendiente de RPC, 501 (ADMIN) |
| POST | /api_rest/v1/reservas | Alta: idVehiculo o patenteVehiculo, fechaHoraInicio/Final |
| PUT | /api_rest/v1/reservas/:id | Cancelar (propietario o ADMIN) |

Alta de cliente admite dni, nombre, apellido, email, telefono, fechaNacimiento y
metadatos.usuario/password. No admite asignar rol, ID o estado desde el navegador.
Las respuestas nunca exponen contraseñas. Errores de gRPC se traducen a HTTP:
INVALID_ARGUMENT→400, NOT_FOUND→404, ALREADY_EXISTS/FAILED_PRECONDITION→409,
UNAUTHENTICATED→401, PERMISSION_DENIED→403, UNAVAILABLE→503,
DEADLINE_EXCEEDED→504, UNIMPLEMENTED→501. Cada RPC tiene un límite de 5 segundos.

## GraphQL

POST `/graphql`, JSON con `query` y `variables`, usando la misma sesión.

```graphql
query {
  reservas(filtros: {estado: CONFIRMADO}) {
    id cliente vehiculo patente fechaInicio fechaFinalizacion importeTotal estado
  }
  miHistorialAlquileres {
    vehiculo patente fechaInicio fechaFinalizacion cantidadDias importeTotal estado
  }
}
```

`miHistorialAlquileres` sólo para CLIENTE. `reservas` acepta DNI (sólo ADMIN),
patenteVehiculo, tipoVehiculo, estado, fechaDesde y fechaHasta; devuelve las propias
para CLIENTE independientemente del DNI enviado.

```graphql
query {
  vehiculosDisponibles(fechaInicio: "2030-11-10T10:00", fechaFinal: "2030-11-12T10:00",
    tipo: SEDAN, precioMax: 40000) {
    patente marca modelo anio color tipoVehiculo precioDiario
  }
}
```

Las fechas son locales ISO sin zona. Disponibilidad usa precisión de minutos porque
así está definido `date_time` en el contrato. El servicio de Vehículos decide cuáles
están disponibles; el Gateway mapea las fechas y filtros, sin recalcular disponibilidad.

## Pruebas y revisión

`npm test` ejecuta 16 pruebas con HTTP real y gRPC de prueba en puertos libres.
Incluyen contratos, filtros, permisos, errores, ciclo alta/cancelación/historial,
formularios publicados y compatibilidad de respuestas de Clientes con/sin `status`.
No hace falta cerrar la demostración para ejecutarlas.

Se comprobó la interfaz en Chromium como ADMIN y CLIENTE, incluyendo formularios,
cancelación e historial. Las capturas y el recorrido para repetirla están en
[docs/evidencias](docs/evidencias/README.md).
Para la entrega faltan las pruebas Java/MySQL detalladas en el documento de
integración. Los puertos de ejemplo para mocks no confirman los puertos de Java.
