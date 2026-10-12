package reservas.services.implementation;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

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

import reservas.repositories.IReservaRepository;

@GrpcService
public class ReservaService extends ReservaServiceGrpc.ReservaServiceImplBase {

    @Autowired
    private IReservaRepository reservaRepository;

    // TODO (INTEGRACIÓN): A futuro, vas a descomentar los clientes gRPC acá arriba
    // @GrpcClient("servicio-vehiculos")
    // private VehiculoServiceGrpc.VehiculoServiceBlockingStub vehiculoStub;
    // @GrpcClient("servicio-clientes")
    // private ClienteServiceGrpc.ClienteServiceBlockingStub clienteStub;

    @Override
    public void crearReserva(CrearReservaRequest request, StreamObserver<Reserva> responseObserver) {
        try {
            LocalDateTime fechaInicio = parsearFecha(request.getFechaHoraInicio(), "fechaHoraInicio");
            LocalDateTime fechaFin = parsearFecha(request.getFechaHoraFinal(), "fechaHoraFinal");

            if (fechaInicio.isBefore(LocalDateTime.now())) {
                throw Status.INVALID_ARGUMENT.withDescription("ERROR: La fecha de inicio debe ser futura.").asRuntimeException();
            }
            if (!fechaFin.isAfter(fechaInicio)) {
                throw Status.INVALID_ARGUMENT.withDescription("ERROR: La fecha de finalización debe ser posterior a la de inicio.").asRuntimeException();
            }

            // =========================================================================
            // TODO (INTEGRACIÓN MICROSERVICIOS): VALIDACIONES EXTERNAS FALTANTES
            // 1. Llamar a ClienteStub para verificar que request.getIdCliente() existe.
            // 2. Llamar a VehiculoStub para verificar que request.getPatenteVehiculo() 
            //    existe y su estado actual es "DISPONIBLE".
            // =========================================================================

            // =========================================================================
            // TODO (INTEGRACIÓN MICROSERVICIOS): PRECIO SIMULADO
            // Este valor deberá venir de la respuesta del VehiculoStub
            double precioDiarioVehiculo = 10000.0; // <-- SIMULADO
            // =========================================================================

            long diasAlquiler = ChronoUnit.DAYS.between(fechaInicio, fechaFin);
            if (diasAlquiler == 0) diasAlquiler = 1;
            double importeTotal = diasAlquiler * precioDiarioVehiculo;

            reservas.models.Reserva dbReserva = new reservas.models.Reserva();
            dbReserva.setIdCliente(request.getIdCliente());
            dbReserva.setPatenteVehiculo(request.getPatenteVehiculo());
            dbReserva.setFechaHoraInicio(fechaInicio);
            dbReserva.setFechaHoraFinal(fechaFin);
            dbReserva.setEstadoReserva(reservas.enums.EstadoReserva.CONFIRMADO);
            dbReserva.setPrecioTotal(importeTotal);

            dbReserva = reservaRepository.save(dbReserva);

            Reserva reservaResponse = Reserva.newBuilder()
                    .setIdReserva(dbReserva.getId())
                    .setPatenteVehiculo(dbReserva.getPatenteVehiculo())
                    .setIdCliente(dbReserva.getIdCliente())
                    .setFechaHoraInicio(dbReserva.getFechaHoraInicio().toString())
                    .setFechaHoraFinal(dbReserva.getFechaHoraFinal().toString())
                    .setImporteTotal(dbReserva.getPrecioTotal())
                    .setEstadoReserva(EstadoReserva.CONFIRMADO)
                    .build();

            responseObserver.onNext(reservaResponse);
            responseObserver.onCompleted();

        } catch (Exception e) {
            manejarError(e, responseObserver, "Error interno al crear reserva: ");
        }
    }

    @Override
    public void cancelarReserva(CancelarReservaRequest request, StreamObserver<CancelarReservaResponse> responseObserver) {
        try {
            Optional<reservas.models.Reserva> reservaOpt = reservaRepository.findById(request.getId());
            
            if (reservaOpt.isEmpty()) {
                throw Status.NOT_FOUND.withDescription("ERROR: Reserva no encontrada.").asRuntimeException();
            }

            reservas.models.Reserva dbReserva = reservaOpt.get();
            LocalDateTime ahora = LocalDateTime.now();

            if (ahora.isAfter(dbReserva.getFechaHoraInicio()) || ahora.isEqual(dbReserva.getFechaHoraInicio())) {
                throw Status.FAILED_PRECONDITION.withDescription("ERROR: No se puede cancelar porque el alquiler ya comenzó.").asRuntimeException();
            }
            if (dbReserva.getEstadoReserva() == reservas.enums.EstadoReserva.CANCELADO) {
                throw Status.FAILED_PRECONDITION.withDescription("ERROR: La reserva ya se encuentra cancelada.").asRuntimeException();
            }

            dbReserva.setEstadoReserva(reservas.enums.EstadoReserva.CANCELADO);
            reservaRepository.save(dbReserva);

            CancelarReservaResponse response = CancelarReservaResponse.newBuilder()
                    .setExito(true)
                    .setEstadoReserva(EstadoReserva.CANCELADO)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            manejarError(e, responseObserver, "Error al cancelar: ");
        }
    }

    @Override
    public void obtenerReserva(ObtenerReservaRequest request, StreamObserver<Reserva> responseObserver) {
        try {
            reservas.models.Reserva dbReserva = reservaRepository.findById(request.getId())
                    .orElseThrow(() -> Status.NOT_FOUND.withDescription("Reserva no encontrada").asRuntimeException());

            Reserva response = mapearAReservaGrpc(dbReserva);
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            manejarError(e, responseObserver, "Error al obtener reserva: ");
        }
    }

    @Override
    public void listarReservas(ListarReservasRequest request, StreamObserver<ListarReservasResponse> responseObserver) {
        try {
            List<reservas.models.Reserva> listaDb = reservaRepository.findAll();
            
            ListarReservasResponse.Builder responseBuilder = ListarReservasResponse.newBuilder();
            
            for (reservas.models.Reserva dbReserva : listaDb) {
                responseBuilder.addReservas(mapearAReservaGrpc(dbReserva));
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            manejarError(e, responseObserver, "Error al listar reservas: ");
        }
    }

    @Override
    public void obtenerMiHistorial(ObtenerHistorialRequest request, StreamObserver<ObtenerHistorialResponse> responseObserver) {
        try {
            List<reservas.models.Reserva> historialDb = reservaRepository.buscarHistorial(
                    request.getIdCliente(),
                    reservas.enums.EstadoReserva.CANCELADO,
                    reservas.enums.EstadoReserva.CONFIRMADO,
                    LocalDateTime.now()
            );

            ObtenerHistorialResponse.Builder responseBuilder = ObtenerHistorialResponse.newBuilder();

            for (reservas.models.Reserva cruda : historialDb) {
                long dias = Math.max(1, ChronoUnit.DAYS.between(cruda.getFechaHoraInicio(), cruda.getFechaHoraFinal()));
                String estadoStr = cruda.getEstadoReserva() == reservas.enums.EstadoReserva.CANCELADO ? "CANCELADO" : "FINALIZADO";

                // =========================================================================
                // TODO (INTEGRACIÓN MICROSERVICIOS): NOMBRE DEL VEHÍCULO SIMULADO
                // A futuro, deberás pedirle a VehiculoStub la "Marca y Modelo" usando 
                // cruda.getPatenteVehiculo() en lugar de armar este String hardcodeado.
                String vehiculoSimulado = "Vehículo " + cruda.getPatenteVehiculo(); // <-- SIMULADO
                // =========================================================================

                HistorialItem item = HistorialItem.newBuilder()
                        .setVehiculo("Vehículo " + cruda.getPatenteVehiculo()) // Simulado hasta conectar con Vehiculos
                        .setPatente(cruda.getPatenteVehiculo())
                        .setFechaHoraInicio(cruda.getFechaHoraInicio().toString())
                        .setFechaHoraFinal(cruda.getFechaHoraFinal().toString())
                        .setDias(Math.toIntExact(dias))
                        .setImporteTotal(cruda.getPrecioTotal())
                        .setEstado(estadoStr)
                        .build();

                responseBuilder.addHistorial(item);
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            manejarError(e, responseObserver, "Error al obtener historial: ");
        }
    }

    private Reserva mapearAReservaGrpc(reservas.models.Reserva dbReserva) {
        EstadoReserva estadoGrpc = dbReserva.getEstadoReserva() == reservas.enums.EstadoReserva.CONFIRMADO 
                ? EstadoReserva.CONFIRMADO : EstadoReserva.CANCELADO;

        return Reserva.newBuilder()
                .setIdReserva(dbReserva.getId())
                .setPatenteVehiculo(dbReserva.getPatenteVehiculo())
                .setIdCliente(dbReserva.getIdCliente())
                .setFechaHoraInicio(dbReserva.getFechaHoraInicio().toString())
                .setFechaHoraFinal(dbReserva.getFechaHoraFinal().toString())
                .setImporteTotal(dbReserva.getPrecioTotal())
                .setEstadoReserva(estadoGrpc)
                .build();
    }

    private LocalDateTime parsearFecha(String valor, String nombreCampo) {
        if (valor == null || valor.isBlank()) {
            throw Status.INVALID_ARGUMENT.withDescription("ERROR: " + nombreCampo + " es obligatorio.").asRuntimeException();
        }
        try {
            return LocalDateTime.parse(valor, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw Status.INVALID_ARGUMENT.withDescription("ERROR: " + nombreCampo + " debe tener formato AAAA-MM-DDT00:00:00.").asRuntimeException();
        }
    }

    private void manejarError(Exception e, StreamObserver<?> responseObserver, String mensajeGenerico) {
        if (e instanceof io.grpc.StatusRuntimeException) {
            responseObserver.onError(e);
        } else {
            responseObserver.onError(Status.INTERNAL.withDescription(mensajeGenerico + e.getMessage()).asRuntimeException());
        }
    }
}