# Integrantes Grupo B
- Juan Martin Diez DNI 46335961
- Arian Savall DNI 46686015
- Joaquin Vadillo DNI 46687269
- Santiago Zurlo DNI 46291432

# Rentar - Gestión de alquiler de vehículos

Aplicación web para administrar clientes, vehículos y reservas de una empresa de alquiler de automóviles.

El sistema ofrece:

- Gestión de clientes.
- Alta, modificación y activación/desactivación de vehículos.
- Consulta de disponibilidad por período y filtros.
- Registro y cancelación de reservas.
- Historial de alquileres.
- Interfaz web con Thymeleaf.
- API REST.
- API GraphQL.
- Autenticación y autorización mediante Spring Security.
- Documentación OpenAPI/Swagger.

## Tecnologías

- Java 17
- Spring Boot 4.1.1
- Spring MVC
- Spring Data JPA
- Spring Security
- Thymeleaf
- MySQL
- REST
- GraphQL
- Lombok
- ModelMapper
- Springdoc OpenAPI
- Maven
- gRPC
- RabbitMQ

## Requisitos

Antes de ejecutar el proyecto es necesario tener instalado:

- JDK 17 o superior
- MySQL 8
- Maven, o utilizar el Maven Wrapper incluido
- Git, opcionalmente

## Configuración de la base de datos

Crear una base de datos MySQL:

```sql
CREATE DATABASE tp_distribuidos;
```

Proponemos el siguiente script para cargar datos mock básicos que servirán para probar todo el proyecto:

```sql
-- 1. Insertar datos en la tabla usuarios_metadatos
-- la password, para todos los usuarios, es 1234.
INSERT INTO usuarios_metadatos (id, password, rol, usuario) VALUES
(1, '$2a$12$1Lk.cZeHax/KlwjH0EfiaurIQ/e.VfWhK/wy0wamJD5dKYrHstsrm', 'ADMIN', 'admin_principal'),
(2, '$2a$12$y1ceyX339xVDc7fUAu8Pxeqwl7Ub37OZNKrDNbogTVWiLwCoe8f2q', 'CLIENTE', 'juanperez99'),
(3, '$2a$12$dVHfIc.kEEq/mxG/Hr.Hr.dcoCc6Sfm3FqbCa73OKKv7ORW/wroQK', 'CLIENTE', 'mariagomez22'),
(4, '$2a$12$SXA9x31zqJDdgCwSuYu4nuDNctHHpXw3AkpVl8TwKK0Nx5Ghg/zSK', 'CLIENTE', 'carloslopez88');

-- 2. Insertar datos en la tabla usuarios
-- Nota: esta_activo es de tipo BIT(1), se suele representar con 1 (true) o 0 (false)
INSERT INTO usuarios (id, apellido, dni, email, esta_activo, fecha_nacimiento, nombre, telefono, metadatos_id) VALUES
(1, 'Admin', '11111111', 'admin@rentacar.com', 1, '1980-01-01', 'Super', '1122334455', 1),
(2, 'Perez', '22222222', 'juan.perez@email.com', 1, '1990-05-15', 'Juan', '1155667788', 2),
(3, 'Gomez', '33333333', 'maria.gomez@email.com', 1, '1985-11-23', 'Maria', '1199887766', 3),
(4, 'Lopez', '44444444', 'carlos.lopez@email.com', 0, '1998-03-10', 'Carlos', '1144556677', 4);

-- 3. Insertar datos en la tabla vehiculos
INSERT INTO vehiculos (id, anio, color, esta_activo, estado, marca, modelo, patente, precio_diario, tipo_vehiculo) VALUES
(1, 2021, 'Rojo', 1, 'DISPONIBLE', 'Toyota', 'Corolla', 'AE123BB', 15000.0, 'SEDAN'),
(2, 2022, 'Blanco', 1, 'EN_ALQUILER', 'Ford', 'Ranger', 'AF456CC', 25000.0, 'PICKUP'),
(3, 2020, 'Gris', 1, 'RESERVADO', 'Volkswagen', 'Golf', 'AD789DD', 18000.0, 'HATCHBACK'),
(4, 2023, 'Negro', 1, 'DISPONIBLE', 'Jeep', 'Compass', 'AG111EE', 30000.0, 'SUV'),
(5, 2019, 'Azul', 1, 'DISPONIBLE', 'Chevrolet', 'Camaro', 'AC222FF', 45000.0, 'COUPE');

-- 4. Insertar datos en la tabla reservas
-- cliente_id hace referencia a usuarios.id
-- vehiculo_id hace referencia a vehiculos.id
INSERT INTO reservas (id_reserva, estado_reserva, fecha_hora_final, fecha_hora_inicio, importe_total, cliente_id, vehiculo_id) VALUES
-- Reserva Confirmada del cliente 2 (Juan Perez) por el vehículo 2 (Ford Ranger, que está 'EN_ALQUILER')
(1, 'CONFIRMADO', '2023-11-10 10:00:00', '2023-11-05 10:00:00', 125000.0, 2, 2),

-- Reserva Confirmada (próxima) del cliente 3 (Maria Gomez) por el vehículo 3 (VW Golf, que está 'RESERVADO')
(2, 'CONFIRMADO', '2023-12-05 15:00:00', '2023-12-01 15:00:00', 72000.0, 3, 3),

-- Reserva Cancelada del cliente 2 (Juan Perez) por el vehículo 1 (Toyota Corolla)
(3, 'CANCELADO', '2023-09-20 18:00:00', '2023-09-18 10:00:00', 30000.0, 2, 1),

-- Reserva histórica ya cumplida (confirmada en el pasado) del cliente 4 (Carlos Lopez) por el vehículo 5
(4, 'CONFIRMADO', '2023-08-15 12:00:00', '2023-08-10 12:00:00', 225000.0, 4, 5);
```

Crear un archivo `.env` en la raíz del proyecto:

```env
DB_URL=jdbc:mysql://localhost:3306/tp_distribuidos
DB_USERNAME=root
DB_PASSWORD=tu_contraseña
SERVER_PORT=8080
```

La aplicación utiliza Hibernate con la configuración `ddl-auto: update`, por lo que las tablas se actualizan automáticamente al iniciar.

Al ejecutar el proyecto con Docker Compose, MySQL monta
`docker/mysql/001-mock-data.sql` y carga estos datos mock automáticamente al crear
el volumen por primera vez. Para volver a ejecutar la carga desde cero, hay que
eliminar el volumen de datos:

```bash
docker compose down -v
docker compose up -d --build
```

La eliminación del volumen borra los datos persistidos de MySQL.

> No incluir el archivo `.env` en el repositorio. Las credenciales deben mantenerse privadas.

## Ejecución

En Windows:

```powershell
`mvnw.cmd` spring-boot:run
```

En Linux o macOS:

```bash
./mvnw spring-boot:run
```

También es posible compilar y ejecutar el archivo generado:

```powershell
.\mvnw.cmd clean package
java -jar target/tp_distribuidos-0.0.1-SNAPSHOT.jar
```

La aplicación estará disponible en:

```text
http://localhost:8080
```

## Acceso web

La aplicación utiliza autenticación mediante formulario:

```text
http://localhost:8080/login
```

Una vez autenticado, el panel principal permite acceder a:

- `/vehiculos`
- `/clientes`
- `/reservas`
- `/historial`

Los roles disponibles son:

- `ADMIN`: administración completa del sistema.
- `CLIENTE`: consulta y gestión de sus propias reservas.

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

Puertos publicados: Vehículos `127.0.0.1:9090`, Clientes
`127.0.0.1:9091`, Reservas `127.0.0.1:9092`, Frontend `127.0.0.1:3000`, RabbitMQ `127.0.0.1:5672`.

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
| PUT | /api_rest/v1/vehiculos/:patente | Pendiente de contrato identificable, 501 (ADMIN) |
| POST | /api_rest/v1/vehiculos/activar | Pendiente de RPC, 501 (ADMIN) |
| POST | /api_rest/v1/reservas | Alta: idVehiculo o patenteVehiculo, fechaHoraInicio/Final |
| PUT | /api_rest/v1/reservas/:id | Cancelar (propietario o ADMIN) |

Alta de cliente admite dni, nombre, apellido, email, telefono, fechaNacimiento y
metadatos.usuario/password. No admite asignar rol, ID o estado desde el navegador.
Las respuestas nunca exponen contraseñas. Errores de gRPC se traducen a HTTP:
INVALID_ARGUMENT→400, NOT_FOUND→404, ALREADY_EXISTS/FAILED_PRECONDITION→409,
UNAUTHENTICATED→401, PERMISSION_DENIED→403, UNAVAILABLE→503,
DEADLINE_EXCEEDED→504, UNIMPLEMENTED→501. Cada RPC tiene un límite de 5 segundos.

## API GraphQL
 
**Endpoint:** `POST http://localhost:3000/graphql`, con cuerpo JSON `{"query": "...", "variables": {...}}`.
 
La identidad del usuario sale siempre de la sesión y nunca de un parámetro, así que ningún filtro amplía los permisos. Los errores se devuelven en el campo `errors`, con un código en `extensions.code`.
 
El schema está documentado con descripciones en el propio código (`api-gateway/src/graphql.js`), y se puede consultar por introspección. Por ejemplo:
 
```json
{ "query": "{ __type(name: \"Reserva\") { description fields { name description } } }" }
```
 
### Consultas disponibles
 
| Consulta | Rol | Qué devuelve |
|---|---|---|
| `vehiculosDisponibles` | <!-- VERIFICAR: ¿exige sesión? --> | Vehículos libres durante todo el período indicado |
| `vehiculos` | <!-- VERIFICAR: ¿exige sesión? --> | Vehículos activos de la flota |
| `reservas` | `ADMIN` y `CLIENTE` | Reservas, con alcance según el rol |
| `miHistorialAlquileres` | solo `CLIENTE` | Historial del cliente de la sesión |
 
### `vehiculosDisponibles`
 
Devuelve los vehículos activos y habilitados que no tienen reservas confirmadas que se solapen con el período. Las reservas canceladas no bloquean.
 
| Parámetro | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `fechaInicio` | String | Sí | Inicio del período. Formato `yyyy-MM-ddTHH:mm`, sin zona horaria |
| `fechaFinal` | String | Sí | Fin del período. Mismo formato, posterior a `fechaInicio` |
| `tipo` | TipoVehiculo | No | `SEDAN`, `SUV`, `PICKUP`, `COUPE` o `HATCHBACK` |
| `marca` | String | No | Marca exacta |
| `modelo` | String | No | Modelo exacto |
| `precioMin` | Float | No | Precio diario mínimo |
| `precioMax` | Float | No | Precio diario máximo. Si se omite, no hay tope |
 
Campos devueltos: `id`, `patente`, `marca`, `modelo`, `anio`, `color`, `tipoVehiculo`, `precioDiario`.
 
Errores: `BAD_USER_INPUT` (fecha inválida), `SERVICIO_VEHICULOS_ERROR`.
 
```graphql
query {
  vehiculosDisponibles(
    fechaInicio: "2028-06-01T10:00"
    fechaFinal: "2028-06-05T10:00"
    tipo: SEDAN
    precioMax: 50000
  ) {
    patente
    marca
    modelo
    tipoVehiculo
    precioDiario
  }
}
```
 
### `reservas`
 
Devuelve las reservas con el nombre del cliente y la marca y modelo del vehículo ya resueltos. El gateway combina datos de los servicios de reservas, clientes y vehículos. Ordena de la reserva más reciente a la más antigua por fecha de inicio.
 
- `ADMIN`: ve las reservas de todos los clientes.
- `CLIENTE`: ve solamente las propias, sin importar los filtros.
Parámetro: `filtros` (opcional), de tipo `FiltroReservasInput`. Los filtros se combinan con AND.
 
| Filtro | Tipo | Descripción | Quién lo aplica |
|---|---|---|---|
| `dniCliente` | String | DNI del cliente | Solo `ADMIN` (un `CLIENTE` lo ignora) |
| `patenteVehiculo` | String | Patente exacta | `ADMIN` y `CLIENTE` |
| `tipoVehiculo` | TipoVehiculo | Tipo del vehículo reservado | `ADMIN` y `CLIENTE` |
| `estado` | EstadoReserva | `CONFIRMADO` o `CANCELADO` | `ADMIN` y `CLIENTE` |
| `fechaDesde` | String | Reservas que terminan en esa fecha o después | `ADMIN` y `CLIENTE` |
| `fechaHasta` | String | Reservas que empiezan en esa fecha o antes | `ADMIN` y `CLIENTE` |
 
Las fechas usan formato ISO local sin zona horaria (`yyyy-MM-ddTHH:mm[:ss]`), y `fechaDesde` no puede ser posterior a `fechaHasta`.
 
Campos devueltos:
 
| Campo | Tipo | Descripción |
|---|---|---|
| `id` | ID | Identificador de la reserva |
| `cliente` | String | Nombre y apellido |
| `vehiculo` | String | Marca y modelo |
| `patente` | String | Patente del vehículo |
| `fechaInicio` | String | Inicio del alquiler |
| `fechaFinalizacion` | String | Fin del alquiler |
| `precioDiario` | Float | Precio por día |
| `importeTotal` | Float | Importe total de la reserva |
| `estado` | EstadoReserva | `CONFIRMADO` o `CANCELADO` |
| `estadoMantenimiento` | String | `PENDIENTE`, `REALIZADO` o `NO_REALIZADO` |
 
Errores: `UNAUTHENTICATED`, `FORBIDDEN`, `BAD_USER_INPUT`, `SERVICIO_RESERVAS_ERROR`. Si falla alguno de los servicios, devuelve un error y no una lista vacía.
 
```graphql
query ($f: FiltroReservasInput) {
  reservas(filtros: $f) {
    cliente
    vehiculo
    patente
    fechaInicio
    importeTotal
    estado
    estadoMantenimiento
  }
}
```
 
Variables:
 
```json
{ "f": { "estado": "CONFIRMADO", "patenteVehiculo": "AC222FF" } }
```
 
Respuesta de ejemplo:
 
```json
{
  "data": {
    "reservas": [
      {
        "cliente": "Super Admin",
        "vehiculo": "Chevrolet Camaro",
        "patente": "AC222FF",
        "fechaInicio": "2027-12-11T11:11",
        "importeTotal": 45000,
        "estado": "CONFIRMADO",
        "estadoMantenimiento": "REALIZADO"
      }
    ]
  }
}
```
 
### `miHistorialAlquileres`
 
Devuelve el historial del cliente autenticado: las reservas canceladas y las reservas confirmadas cuyo período ya terminó. Va ordenado de la más reciente a la más antigua. No recibe parámetros: el cliente sale de la sesión.
 
| Campo | Tipo | Descripción |
|---|---|---|
| `vehiculo` | String | Marca y modelo |
| `patente` | String | Patente del vehículo |
| `fechaInicio` | String | Inicio del alquiler |
| `fechaFinalizacion` | String | Fin del alquiler |
| `cantidadDias` | Int | Días del alquiler, con mínimo de 1 |
| `importeTotal` | Float | Importe total |
| `estado` | String | `CANCELADO`, o `FINALIZADO` (confirmada con período terminado) |
 
`FINALIZADO` no se guarda en la base: se deriva de la fecha de fin.
 
Errores: `UNAUTHENTICATED`, `FORBIDDEN` (un `ADMIN` no puede usarla), `SERVICIO_RESERVAS_ERROR`.
 
```graphql
query {
  miHistorialAlquileres {
    vehiculo
    patente
    fechaInicio
    cantidadDias
    importeTotal
    estado
  }
}
```
 
Respuesta de ejemplo:
 
```json
{
  "data": {
    "miHistorialAlquileres": [
      {
        "vehiculo": "Chevrolet Camaro",
        "patente": "AC222FF",
        "fechaInicio": "2027-10-30T11:11",
        "cantidadDias": 1,
        "importeTotal": 45000,
        "estado": "CANCELADO"
      },
      {
        "vehiculo": "Chevrolet Camaro",
        "patente": "AC222FF",
        "fechaInicio": "2026-10-10T07:00",
        "cantidadDias": 2,
        "importeTotal": 90000,
        "estado": "FINALIZADO"
      }
    ]
  }
}
```
 
Error de ejemplo (un `ADMIN` pide el historial):
 
```json
{
  "errors": [
    { "message": "El historial requiere el rol CLIENTE", "extensions": { "code": "FORBIDDEN" } }
  ],
  "data": null
}
```
 
### Restricciones de acceso por rol
 
| Consulta | Sin sesión | `ADMIN` | `CLIENTE` |
|---|---|---|---|
| `reservas` | `UNAUTHENTICATED` | Todas las reservas | Solo las propias |
| `miHistorialAlquileres` | `UNAUTHENTICATED` | `FORBIDDEN` | Su historial |

## Documentación OpenAPI

Con la aplicación iniciada, la documentación de la API REST puede consultarse en:

```text
http://localhost:8080/swagger-ui/index.html
```

El documento OpenAPI está disponible en:

```text
http://localhost:8080/v3/api-docs
```

## Estructura principal

```text
src/
├── main/
│   ├── java/unla/tp/tp_distribuidos/
│   │   ├── configuration/     Configuración de seguridad
│   │   ├── controllers/       Controladores web, REST y GraphQL
│   │   ├── dtos/              Objetos de transferencia
│   │   ├── clientes.enums/             Enumeraciones del dominio
│   │   ├── clientes.entities/            Entidades JPA
│   │   ├── clientes.repositories/      Repositorios de persistencia
│   │   └── services/          Lógica de negocio
│   └── resources/
│       ├── graphql/            Esquemas GraphQL
│       ├── static/             CSS y recursos estáticos
│       ├── templates/          Vistas Thymeleaf
│       └── clientes.application.yaml   Configuración de la aplicación
└── test/
    └── java/                   Pruebas automatizadas
```

## Ejecutar las pruebas

En Windows:

```powershell
.\mvnw.cmd test
```

En Linux o macOS:

```bash
./mvnw test
```

## Notas

- Las contraseñas se almacenan utilizando BCrypt.
- Las rutas REST y GraphQL requieren autenticación.
- La base de datos debe estar disponible antes de iniciar la aplicación.
- Los esquemas GraphQL se encuentran en `graphql`.
- La configuración sensible se obtiene desde variables de entorno mediante `.env`.
