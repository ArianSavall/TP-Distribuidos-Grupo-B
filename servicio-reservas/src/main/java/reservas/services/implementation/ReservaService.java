package reservas.services.implementation;

import grpc.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import reservas.models.Reserva;
import reservas.enums.EstadoReserva;
import reservas.repositories.IReservaRepository;
import reservas.mappers.Parsers;
import reservas.mappers.ReservaMapper;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@GrpcService
public class ReservaService extends ReservaServiceGrpc.ReservaServiceImplBase {

    @Autowired
    private IReservaRepository reservaRepository;

    @GrpcClient("servicio-vehiculos")
    private VehiculoServiceGrpc.VehiculoServiceBlockingStub vehiculoStub;

    @Override
    public void crearReserva(CrearReservaRequest request, StreamObserver<grpc.Reserva> responseObserver) {
        try {
            LocalDateTime fechaInicio = Parsers.fechaHora(request.getFechaHoraInicio(), "fechaHoraInicio");
            LocalDateTime fechaFin = Parsers.fechaHora(request.getFechaHoraFinal(), "fechaHoraFinal");

            if (fechaInicio.isBefore(LocalDateTime.now()) || !fechaFin.isAfter(fechaInicio)) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Fechas inválidas. La fecha de inicio debe ser futura y la final posterior a la de inicio.")
                        .asRuntimeException());
                return;
            }

            GetVehiculoPatenteRequest vehiculoReq = GetVehiculoPatenteRequest.newBuilder()
                    .setPatente(request.getPatenteVehiculo())
                    .build();

            grpc.Vehiculo vehiculoResponse = vehiculoStub.getVehiculoByPatente(vehiculoReq);

            if (!vehiculoResponse.getEstado().equalsIgnoreCase("DISPONIBLE") || !vehiculoResponse.getEstaActivo()) {
                responseObserver.onError(Status.FAILED_PRECONDITION
                        .withDescription("El vehículo no está disponible o está inactivo.")
                        .asRuntimeException());
                return;
            }

            long diasAlquiler = Math.max(1, ChronoUnit.DAYS.between(fechaInicio, fechaFin));
            double importeTotal = diasAlquiler * vehiculoResponse.getPrecioDiario();

            Reserva nuevaReserva = new Reserva();
            nuevaReserva.setIdCliente(request.getIdCliente());
            nuevaReserva.setPatenteVehiculo(request.getPatenteVehiculo());
            nuevaReserva.setFechaHoraInicio(fechaInicio);
            nuevaReserva.setFechaHoraFinal(fechaFin);
            nuevaReserva.setEstadoReserva(EstadoReserva.CONFIRMADO);
            nuevaReserva.setPrecioTotal(importeTotal);

            Reserva guardada = reservaRepository.save(nuevaReserva);
            grpc.Reserva grpcResponse = ReservaMapper.toProto(guardada);

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();

        } catch (io.grpc.StatusRuntimeException e) {
            responseObserver.onError(e);
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error al crear la reserva: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void obtenerReserva(ObtenerReservaRequest request, StreamObserver<grpc.Reserva> responseObserver) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(request.getId());

        if (reservaOpt.isPresent()) {
            responseObserver.onNext(ReservaMapper.toProto(reservaOpt.get()));
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("La reserva no existe en la base de datos.")
                    .asRuntimeException());
        }
    }

    @Override
    public void listarReservas(ListarReservasRequest request, StreamObserver<ListarReservasResponse> responseObserver) {
        try {
            List<Reserva> reservas = reservaRepository.findAll();
            List<grpc.Reserva> reservasGrpc = new ArrayList<>();

            for (Reserva r : reservas) {
                reservasGrpc.add(ReservaMapper.toProto(r));
            }

            ListarReservasResponse response = ListarReservasResponse.newBuilder()
                    .addAllReservas(reservasGrpc)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error al listar las reservas: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void cancelarReserva(CancelarReservaRequest request, StreamObserver<CancelarReservaResponse> responseObserver) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(request.getId());

        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();

            if (reserva.getEstadoReserva() == EstadoReserva.CANCELADO) {
                responseObserver.onError(Status.FAILED_PRECONDITION
                        .withDescription("La reserva ya se encuentra cancelada.")
                        .asRuntimeException());
                return;
            }

            reserva.setEstadoReserva(EstadoReserva.CANCELADO);
            reservaRepository.save(reserva);

            CancelarReservaResponse response = CancelarReservaResponse.newBuilder()
                    .setExito(true)
                    .setEstadoReserva(grpc.EstadoReserva.CANCELADO)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("La reserva a cancelar no existe.")
                    .asRuntimeException());
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

                HistorialItem item = HistorialItem.newBuilder()
                        .setVehiculo("Vehículo " + cruda.getPatenteVehiculo()) 
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
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error al obtener historial: " + e.getMessage())
                    .asRuntimeException());
        }
    }

@Override
    public void getVehiculosOcupados(FechasRequest request, StreamObserver<VehiculosOcupadosResponse> responseObserver) {
        try {
            LocalDateTime fechaDesde = Parsers.fechaHora(request.getFechaDesde(), "fecha_desde");
            LocalDateTime fechaHasta = Parsers.fechaHora(request.getFechaHasta(), "fecha_hasta");

            List<String> patentesOcupadas = reservaRepository.findPatentesOcupadasEnRango(
                    fechaDesde, 
                    fechaHasta, 
                    reservas.enums.EstadoReserva.CANCELADO
            );

            VehiculosOcupadosResponse response = VehiculosOcupadosResponse.newBuilder()
                    .addAllPatentes(patentesOcupadas)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error al obtener vehículos ocupados: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}