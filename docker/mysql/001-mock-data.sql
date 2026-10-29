CREATE TABLE IF NOT EXISTS usuarios_metadatos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuarios_metadatos_usuario (usuario)
);

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dni VARCHAR(255) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    apellido VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    telefono VARCHAR(255),
    fecha_nacimiento DATE,
    esta_activo BIT(1) NOT NULL,
    metadatos_id BIGINT,
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuarios_dni (dni),
    UNIQUE KEY uk_usuarios_email (email),
    UNIQUE KEY uk_usuarios_metadatos (metadatos_id),
    CONSTRAINT fk_usuarios_metadatos
        FOREIGN KEY (metadatos_id) REFERENCES usuarios_metadatos (id)
);

CREATE TABLE IF NOT EXISTS vehiculos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    patente VARCHAR(255) NOT NULL,
    marca VARCHAR(255) NOT NULL,
    modelo VARCHAR(255) NOT NULL,
    anio INT NOT NULL,
    color VARCHAR(255),
    tipo_vehiculo VARCHAR(255) NOT NULL,
    precio_diario DOUBLE NOT NULL,
    estado VARCHAR(255) NOT NULL,
    esta_activo BIT(1) NOT NULL,
    resultado_verificacion VARCHAR(255) NOT NULL,
    resultado_validacion_patente VARCHAR(255),
    resultado_validacion_titular VARCHAR(255),
    habilitado BIT(1) NOT NULL,
    cliente_id BIGINT,
    PRIMARY KEY (id),
    UNIQUE KEY uk_vehiculos_patente (patente)
);

CREATE TABLE IF NOT EXISTS reservas (
    id_reserva BIGINT NOT NULL AUTO_INCREMENT,
    id_cliente BIGINT NOT NULL,
    patente_vehiculo VARCHAR(255) NOT NULL,
    fecha_hora_inicio DATETIME(6) NOT NULL,
    fecha_hora_final DATETIME(6) NOT NULL,
    estado_reserva VARCHAR(255) NOT NULL,
    precio_total DOUBLE NOT NULL,
    estado_mantenimiento VARCHAR(255),
    PRIMARY KEY (id_reserva)
);

INSERT INTO usuarios_metadatos (id, usuario, password, rol)
VALUES
    (1, 'admin_principal', '$2a$12$1Lk.cZeHax/KlwjH0EfiaurIQ/e.VfWhK/wy0wamJD5dKYrHstsrm', 'ADMIN'),
    (2, 'juanperez99', '$2a$12$y1ceyX339xVDc7fUAu8Pxeqwl7Ub37OZNKrDNbogTVWiLwCoe8f2q', 'CLIENTE'),
    (3, 'mariagomez22', '$2a$12$dVHfIc.kEEq/mxG/Hr.Hr.dcoCc6Sfm3FqbCa73OKKv7ORW/wroQK', 'CLIENTE'),
    (4, 'carloslopez88', '$2a$12$SXA9x31zqJDdgCwSuYu4nuDNctHHpXw3AkpVl8TwKK0Nx5Ghg/zSK', 'CLIENTE');

INSERT INTO usuarios (
    id, apellido, dni, email, esta_activo, fecha_nacimiento, nombre, telefono, metadatos_id
)
VALUES
    (1, 'Admin', '11111111', 'admin@rentacar.com', 1, '1980-01-01', 'Super', '1122334455', 1),
    (2, 'Perez', '22222222', 'juan.perez@email.com', 1, '1990-05-15', 'Juan', '1155667788', 2),
    (3, 'Gomez', '33333333', 'maria.gomez@email.com', 1, '1985-11-23', 'Maria', '1199887766', 3),
    (4, 'Lopez', '44444444', 'carlos.lopez@email.com', 1, '1998-03-10', 'Carlos', '1144556677', 4);

INSERT INTO vehiculos (
    id, anio, color, esta_activo, estado, habilitado, marca, modelo, patente,
    precio_diario, tipo_vehiculo, resultado_verificacion
)
VALUES
    (1, 2021, 'Rojo', 1, 'DISPONIBLE', 1, 'Toyota', 'Corolla', 'AE123BB', 15000.0, 'SEDAN', 'VALIDADO'),
    (2, 2022, 'Blanco', 1, 'EN_ALQUILER', 1, 'Ford', 'Ranger', 'AF456CC', 25000.0, 'PICKUP', 'VALIDADO'),
    (3, 2020, 'Gris', 1, 'RESERVADO', 1, 'Volkswagen', 'Golf', 'AD789DD', 18000.0, 'HATCHBACK', 'VALIDADO'),
    (4, 2023, 'Negro', 1, 'DISPONIBLE', 1, 'Jeep', 'Compass', 'AG111EE', 30000.0, 'SUV', 'VALIDADO'),
    (5, 2019, 'Azul', 1, 'DISPONIBLE', 1, 'Chevrolet', 'Camaro', 'AC222FF', 45000.0, 'COUPE', 'VALIDADO');

INSERT INTO reservas (
    id_reserva, id_cliente, patente_vehiculo, fecha_hora_final, fecha_hora_inicio,
    precio_total, estado_reserva, estado_mantenimiento
)
VALUES
    (1, 2, 'AF456CC', '2026-11-10 10:00:00', '2026-11-05 10:00:00', 125000.0, 'CONFIRMADO', 'PENDIENTE'),
    (2, 3, 'AD789DD', '2026-12-05 15:00:00', '2026-12-01 15:00:00', 72000.0, 'CONFIRMADO', 'PENDIENTE'),
    (3, 2, 'AE123BB', '2026-09-20 18:00:00', '2026-09-18 10:00:00', 30000.0, 'CANCELADO', 'PENDIENTE'),
    (4, 4, 'AC222FF', '2026-08-15 12:00:00', '2026-08-10 12:00:00', 225000.0, 'CONFIRMADO', 'PENDIENTE');
