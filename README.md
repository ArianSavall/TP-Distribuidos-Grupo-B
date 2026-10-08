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

## API REST

### Clientes

URL base:

```text
/api_rest/v1/clientes
```

Operaciones disponibles:

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api_rest/v1/clientes` | Listar clientes |
| GET | `/api_rest/v1/clientes/buscar/id/{id}` | Buscar cliente por ID |
| GET | `/api_rest/v1/clientes/buscar/dni/{dni}` | Buscar cliente por DNI |
| POST | `/api_rest/v1/clientes` | Crear cliente |
| PATCH | `/api_rest/v1/clientes/modificar/id/{id}` | Modificar cliente |
| PATCH | `/api_rest/v1/clientes/modificar/dni` | Modificar cliente por DNI |
| DELETE | `/api_rest/v1/clientes/borrar/id/{id}` | Dar de baja por ID |
| DELETE | `/api_rest/v1/clientes/borrar/dni/{dni}` | Dar de baja por DNI |

### Vehículos

URL base:

```text
/api_rest/v1/vehiculos
```

Operaciones disponibles:

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api_rest/v1/vehiculos/getAll` | Listar vehículos |
| GET | `/api_rest/v1/vehiculos/{patente}` | Buscar vehículo por patente |
| POST | `/api_rest/v1/vehiculos/create` | Crear vehículo |
| PUT | `/api_rest/v1/vehiculos/{patente}` | Actualizar vehículo |
| POST | `/api_rest/v1/vehiculos/activar` | Activar vehículo |
| POST | `/api_rest/v1/vehiculos/desactivar` | Desactivar vehículo |

### Reservas

URL base:

```text
/api/v1/reservas
```

Operaciones disponibles:

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/v1/reservas` | Crear reserva |
| PUT | `/api/v1/reservas/{id}/cancelar` | Cancelar reserva |

Las APIs REST requieren autenticación.

## API GraphQL

Endpoint principal:

```text
http://localhost:8080/graphql
```

Interfaz GraphiQL:

```text
http://localhost:8080/graphiql
```

### Consultar vehículos disponibles

```graphql
query {
  vehiculosDisponibles(
    tipo: SUV
    marca: "Toyota"
    modelo: "Corolla"
    precioMin: 10000
    precioMax: 50000
    fechaInicio: "2026-10-01T10:00:00"
    fechaFinal: "2026-10-05T10:00:00"
  ) {
    patente
    marca
    modelo
    anio
    color
    tipoVehiculo
    precioDiario
  }
}
```

### Consultar reservas

```graphql
query {
  reservas(
    filtros: {
      patenteVehiculo: "ABC123"
      tipoVehiculo: SUV
      estado: CONFIRMADO
      fechaDesde: "2026-10-01T00:00:00"
      fechaHasta: "2026-10-31T23:59:59"
    }
  ) {
    id
    cliente
    vehiculo
    patente
    fechaInicio
    fechaFinalizacion
    precioDiario
    importeTotal
    estado
  }
}
```

Los administradores pueden filtrar reservas por `dniCliente`. Los clientes autenticados solo pueden consultar sus propias reservas.

### Consultar historial del cliente

```graphql
query {
  miHistorialAlquileres {
    vehiculo
    patente
    fechaInicio
    fechaFinalizacion
    cantidadDias
    importeTotal
    estado
  }
}
```

Esta consulta requiere el rol `CLIENTE`.

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
│   │   ├── enums/             Enumeraciones del dominio
│   │   ├── models/            Entidades JPA
│   │   ├── repositories/      Repositorios de persistencia
│   │   └── services/          Lógica de negocio
│   └── resources/
│       ├── graphql/            Esquemas GraphQL
│       ├── static/             CSS y recursos estáticos
│       ├── templates/          Vistas Thymeleaf
│       └── application.yaml   Configuración de la aplicación
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
