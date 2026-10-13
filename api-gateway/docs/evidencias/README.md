# Evidencias de prueba del Gateway

Fecha: 12/10/2026. Entorno: Node.js 24, navegador Chromium automatizado,
Gateway HTTP real y servidores gRPC **simulados**, sin MySQL ni servicios Java.

- `01-vehiculos-simulados.png`: lista de vehículos como ADMIN, formulario original adaptado.
- `02-reservas-simuladas.png`: disponibilidad, alta y consulta como CLIENTE.
- `03-historial-simulado.png`: reserva cancelada por el cliente y alquiler finalizado.
- `04-reservas-movil-simuladas.png`: adaptación a un ancho de 390 píxeles; las tablas
  admiten desplazamiento horizontal.

El recorrido automatizado también comprobó alta de vehículo, alta/modificación de
cliente, cierre de sesión, ocultación de administración para CLIENTE, disponibilidad,
alta de reserva por patente, cancelación y consulta del historial. No hubo errores
JavaScript en el navegador durante el recorrido.

Estas capturas documentan el Gateway y su interfaz con simuladores. Deben complementarse
con capturas de la ejecución contra los servicios Java y bases del grupo antes de
presentar el sistema como integrado.

## Repetir manualmente

1. Desde `api-gateway`, ejecutar `npm ci`, `npm test` y `npm run demo`.
2. Abrir http://localhost:3000. Usar `admin_prueba` / `clave-prueba` para gestión.
3. Registrar un vehículo con patente nueva y un cliente con DNI/usuario nuevos;
   buscar ambos y editar el nombre del cliente.
4. Cerrar sesión y entrar como `cliente_prueba` / `clave-prueba`.
5. Ir a Reservas, consultar un período futuro y registrar una reserva por patente.
6. Cancelar la reserva y abrir Mi historial. Verificar CANCELADO.
7. Entrar como `cliente_dos`: no debe ver ni cancelar las reservas del primero.
8. Edición/reactivación de vehículos muestran error 501 por las limitaciones del
   contrato publicado; no considerar esas dos operaciones aprobadas.
