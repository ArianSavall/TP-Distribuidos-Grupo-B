# Punto 7 — Historial de alquileres con GraphQL

## Objetivo
Permitir que un cliente autenticado consulte su propio historial
de alquileres finalizados y reservas canceladas.

## Acceso
- Pantalla: http://localhost:8080/historial
- Endpoint: POST /graphql
- Autenticación: HTTP Basic con una cuenta activa del sistema.
- Rol requerido: CLIENTE.
- El usuario se obtiene de la autenticación; no se recibe un ID
  de cliente como argumento.

## Consulta
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

## Criterios
Incluye todas las reservas CANCELADO del cliente.
Incluye reservas CONFIRMADO cuya fecha de finalización
sea menor o igual al momento de la consulta.
Estas últimas se muestran como FINALIZADO, sin modificar
el estado almacenado ni el enum existente.
Excluye reservas confirmadas futuras o todavía en curso.
Ordena por fecha de inicio descendente y luego por ID descendente.
Calcula días completos entre inicio y fin, con mínimo de uno,
siguiendo el criterio del alta de reservas existente.
Devuelve el importe guardado en la reserva.
Devuelve fechas ISO sin zona horaria.

## Organización
HistorialWebController entrega la vista historial.html.
La vista consulta GraphQL y muestra los resultados.
HistorialAlquilerController recibe la consulta GraphQL.
HistorialAlquilerService comprueba el rol y arma los DTO.
IReservaRepository filtra por usuario, estado y fecha.
historial.graphqls documenta la consulta y sus campos.

## Configuración local

Se utiliza un archivo .env ignorado por Git.
En el entorno local probado, DB_URL usa connectionTimeZone=LOCAL
en lugar de serverTimezone=UTC para evitar el desfase observado
entre MySQL y Java. Se verificó que una hora guardada como 10:00
se muestra como 10:00. Cada entorno debe mantener una configuración
horaria coherente.

La carpeta graphql-client se conserva mediante .gitkeep porque
el generador configurado en el pom.xml requiere que exista.

## Verificaciones realizadas
mvnw.cmd test: 1 test de arranque, sin fallos ni errores.
Cliente sin historial: mensaje de lista vacía.
Reserva cancelada y alquiler finalizado: visibles, con días
e importes esperados.
Reserva confirmada futura: guardada en MySQL y excluida de la vista.
Segundo cliente sin reservas: no ve las del primero.
Consulta GraphQL anónima: HTTP 401, sin datos.
Consulta GraphQL con ADMIN: error FORBIDDEN y data: null.
Horas: corregidas y verificadas tras ajustar la conexión local.

Las verificaciones funcionales y de permisos fueron manuales.
El test automatizado existente solo comprueba el arranque.

## Alcance y revisión

Se conservaron las entidades y los servicios existentes.
Se agregó una consulta a IReservaRepository y reglas de seguridad
para las nuevas rutas.

ReservaRestController se trasladó de la carpeta api_rest.v1
a api_rest/v1 para coincidir con su paquete, sin cambiar su contenido.

El criterio de FINALIZADO derivado de la fecha debe ser revisado
por el equipo, ya que el enum actual solo tiene CONFIRMADO y CANCELADO.

Los datos utilizados son ficticios y fueron cargados únicamente
en MySQL local.

La integración a develop queda pendiente de aprobación del compañero.