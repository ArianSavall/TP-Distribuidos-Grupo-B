package unla.tp.tp_distribuidos.reservas.services.implementation;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import net.devh.boot.grpc.client.inject.GrpcClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

// Importaciones de las clases autogeneradas por tu archivo ReservaProto.proto
import grpc.ReservaServiceGrpc;
import grpc.CrearReservaRequest;
import grpc.ObtenerReservaRequest;
import grpc.ListarReservasRequest;
import grpc.ListarReservasResponse;
import grpc.CancelarReservaRequest;
import grpc.CancelarReservaResponse;
import grpc.ObtenerHistorialRequest;
import grpc.ObtenerHistorialResponse;
import grpc.HistorialItem;
import grpc.Reserva;
import grpc.EstadoReserva;

@GrpcService
public class ReservaService extends ReservaServiceGrpc.ReservaServiceImplBase {

    // INYECCIÓN DEL CLIENTE gRPC PARA HABLAR CON SERVICIO-VEHICULOS
    // TODO: Descomentar cuando el servicio-vehiculos y su proto estén listos
    // @GrpcClient("servicio-vehiculos")
    // private VehiculoServiceGrpc.VehiculoServiceBlockingStub vehiculoStub;

    @Override
    public void crearReserva(CrearReservaRequest request, StreamObserver<Reserva> responseObserver) {
        try {
            // 1. Validar fechas enviadas como String en el Request
            LocalDateTime fechaInicio = parsearFecha(request.getFechaHoraInicio(), "fechaHoraInicio");
            LocalDateTime fechaFin = parsearFecha(request.getFechaHoraFinal(), "fechaHoraFinal");

            if (fechaInicio.isBefore(LocalDateTime.now())) {
                throw Status.INVALID_ARGUMENT.withDescription("ERROR: La fecha de inicio debe ser futura.").asRuntimeException();
            }
            if (!fechaFin.isAfter(fechaInicio)) {
                throw Status.INVALID_ARGUMENT.withDescription("ERROR: La fecha de finalización debe ser posterior a la de inicio.").asRuntimeException();
            }

            // 2. VALIDACIONES EXTERNAS (Vía gRPC a otros microservicios)
            // TODO: Llamar a servicio-clientes para validar que request.getIdCliente() existe y está activo.
            // TODO: Llamar a servicio-vehiculos para validar request.getPatenteVehiculo(), comprobar que está activo y obtener su precioDiario.
            // TODO: Llamar a servicio-database para contar reservas solapadas y asegurar disponibilidad.
            
            // Simulación del precio obtenido del servicio de vehículos (para que la lógica compile temporalmente)
            double precioDiarioVehiculo = 10000.0; 

            // 3. Lógica de negocio (Cálculo de días e importe total)
            long diasAlquiler = ChronoUnit.DAYS.between(fechaInicio, fechaFin);
            if (diasAlquiler == 0) diasAlquiler = 1;
            double importeTotal = diasAlquiler * precioDiarioVehiculo;

            // 4. PERSISTENCIA (Vía gRPC)
            // TODO: Llamar a servicio-database para guardar esta reserva y que nos devuelva el ID generado.
            long idGeneradoPorBD = 999; // ID simulado hasta conectar la BD

            // 5. Mapear resultado al mensaje Reserva de Protobuf
            Reserva reservaResponse = Reserva.newBuilder()
                    .setIdReserva(idGeneradoPorBD)
                    .setPatenteVehiculo(request.getPatenteVehiculo())
                    .setIdCliente(request.getIdCliente())
                    .setFechaHoraInicio(request.getFechaHoraInicio())
                    .setFechaHoraFinal(request.getFechaHoraFinal())
                    .setImporteTotal(importeTotal)
                    .setEstadoReserva(EstadoReserva.CONFIRMADO)
                    .build();

            // 6. Enviar respuesta al cliente y cerrar comunicación
            responseObserver.onNext(reservaResponse);
            responseObserver.onCompleted();

        } catch (Exception e) {
            if (e instanceof io.grpc.StatusRuntimeException) {
                responseObserver.onError(e);
            } else {
                responseObserver.onError(Status.INTERNAL.withDescription("Error interno: " + e.getMessage()).asRuntimeException());
            }
        }
    }

    @Override
    public void cancelarReserva(CancelarReservaRequest request, StreamObserver<CancelarReservaResponse> responseObserver) {
        try {
            // 1. OBTENER RESERVA ACTUAL (Vía gRPC)
            // TODO: Llamar a servicio-database pasándole request.getId() para traer los datos de la reserva.
            
            // Lógica simulada para validar la cancelación (deberás usar los datos reales de la BD)
            LocalDateTime fechaInicioSimulada = LocalDateTime.now().plusDays(2); // Simulación
            EstadoReserva estadoActualSimulado = EstadoReserva.CONFIRMADO; // Simulación

            LocalDateTime ahora = LocalDateTime.now();
            if (ahora.isAfter(fechaInicioSimulada) || ahora.isEqual(fechaInicioSimulada)) {
                throw Status.FAILED_PRECONDITION.withDescription("ERROR: No se puede cancelar la reserva porque el período de alquiler ya ha comenzado.").asRuntimeException();
            }
            if (estadoActualSimulado == EstadoReserva.CANCELADO) {
                throw Status.FAILED_PRECONDITION.withDescription("ERROR: La reserva ya se encuentra cancelada.").asRuntimeException();
            }

            // 2. ACTUALIZAR ESTADO (Vía gRPC)
            // TODO: Llamar a servicio-database para hacer un UPDATE del estado a CANCELADO para el request.getId().

            // 3. Responder éxito
            CancelarReservaResponse response = CancelarReservaResponse.newBuilder()
                    .setExito(true)
                    .setEstadoReserva(EstadoReserva.CANCELADO)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            if (e instanceof io.grpc.StatusRuntimeException) {
                responseObserver.onError(e);
            } else {
                responseObserver.onError(Status.INTERNAL.withDescription("Error al cancelar: " + e.getMessage()).asRuntimeException());
            }
        }
    }

    @Override
    public void obtenerReserva(ObtenerReservaRequest request, StreamObserver<Reserva> responseObserver) {
        try {
            // TODO: Llamar a servicio-database pasando request.getId() y retornar el objeto Reserva.
            
            // Simulación de respuesta vacía por ahora
            responseObserver.onNext(Reserva.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void listarReservas(ListarReservasRequest request, StreamObserver<ListarReservasResponse> responseObserver) {
        try {
            // TODO: Llamar a servicio-database para pedir la lista de reservas y mapearla al ListarReservasResponse.
            
            // Simulación de respuesta vacía por ahora
            responseObserver.onNext(ListarReservasResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void obtenerMiHistorial(ObtenerHistorialRequest request, StreamObserver<ObtenerHistorialResponse> responseObserver) {
        try {
            long idCliente = request.getIdCliente();

            // 1. BUSCAR RESERVAS CRUDAS EN LA BD (Vía gRPC a servicio-database)
            // TODO: Llamar a servicio-database para traer las reservas de este idCliente que estén CONFIRMADO o CANCELADO

            ObtenerHistorialResponse.Builder responseBuilder = ObtenerHistorialResponse.newBuilder();

            // 2. BUCLE DE ENRIQUECIMIENTO (Trabajo pesado)
            /*
            for (Reserva cruda : reservasCrudas) {
                
                // TODO: Llamar al vehiculoStub pasándole cruda.getPatenteVehiculo()
                // ObtenerVehiculoResponse datosVeh = vehiculoStub.obtenerVehiculo(...);
                String marca = "Simulada"; // datosVeh.getMarca()
                String modelo = "Simulada"; // datosVeh.getModelo()

                LocalDateTime inicio = parsearFecha(cruda.getFechaHoraInicio(), "inicio");
                LocalDateTime fin = parsearFecha(cruda.getFechaHoraFinal(), "fin");
                
                long dias = Math.max(1, ChronoUnit.DAYS.between(inicio, fin));
                
                String estadoStr = cruda.getEstadoReserva() == EstadoReserva.CANCELADO ? "CANCELADO" : "FINALIZADO";

                HistorialItem item = HistorialItem.newBuilder()
                        .setVehiculo(marca + " " + modelo)
                        .setPatente(cruda.getPatenteVehiculo())
                        .setFechaHoraInicio(cruda.getFechaHoraInicio())
                        .setFechaHoraFinal(cruda.getFechaHoraFinal())
                        .setDias(Math.toIntExact(dias))
                        .setImporteTotal(cruda.getImporteTotal())
                        .setEstado(estadoStr)
                        .build();

                responseBuilder.addHistorial(item);
            }
            */

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Error al obtener historial: " + e.getMessage()).asRuntimeException());
        }
    }

    private LocalDateTime parsearFecha(String valor, String nombreCampo) {
        if (valor == null || valor.isBlank()) {
            throw Status.INVALID_ARGUMENT.withDescription("ERROR: " + nombreCampo + " es obligatorio.").asRuntimeException();
        }
        try {
            return LocalDateTime.parse(valor, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw Status.INVALID_ARGUMENT.withDescription("ERROR: " + nombreCampo + " debe tener formato AAAA-MM-DDT00:00:00).").asRuntimeException();
        }
    }
}