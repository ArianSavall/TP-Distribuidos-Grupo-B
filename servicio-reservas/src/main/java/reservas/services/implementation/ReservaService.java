package reservas.services.implementation;

import grpc.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import reservas.config.RabbitMQConfig;
import reservas.dtos.ReservaConfirmadaEvent;

import reservas.models.Reserva;
import reservas.enums.EstadoReserva;
import reservas.repositories.IReservaRepository;
import reservas.mappers.Parsers;
import reservas.mappers.ReservaMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@GrpcService
public class ReservaService extends ReservaServiceGrpc.ReservaServiceImplBase {

    @Autowired
    private IReservaRepository reservaRepository;

    @GrpcClient("servicio-vehiculos")
    private VehiculoServiceGrpc.VehiculoServiceBlockingStub vehiculoStub;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void crearReserva(CrearReservaRequest request, StreamObserver<CrearReservaResponse> responseObserver) {
        try {
            LocalDateTime fechaInicio = Parsers.fechaHora(request.getFechaHoraInicio(), "fechaHoraInicio");
            LocalDateTime fechaFin = Parsers.fechaHora(request.getFechaHoraFinal(), "fechaHoraFinal");

            if (fechaInicio.isBefore(LocalDateTime.now()) || !fechaFin.isAfter(fechaInicio)) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Fechas inválidas. La fecha de inicio debe ser futura y la final posterior a la de inicio.")
                        .asRuntimeException());
                return;
            }

            GetVehiculoByPatenteRequest vehiculoReq = GetVehiculoByPatenteRequest.newBuilder()
                    .setPatente(request.getPatenteVehiculo())
                    .build();

            grpc.GetVehiculoByPatenteResponse vehiculoResponse = vehiculoStub.getVehiculoByPatente(vehiculoReq);

            if (!vehiculoResponse.getEstado().name().equalsIgnoreCase("DISPONIBLE") || !vehiculoResponse.getEstaActivo()) {
                responseObserver.onError(Status.FAILED_PRECONDITION
                        .withDescription("El vehículo no está disponible o está inactivo.")
                        .asRuntimeException());
                return;
            }
            
             long solapadas = reservaRepository.countReservasSolapadas(
                    request.getPatenteVehiculo(),
                    EstadoReserva.CONFIRMADO,
                    fechaInicio,
                    fechaFin
            );

            if (solapadas > 0) {
                responseObserver.onError(Status.FAILED_PRECONDITION
                        .withDescription("El vehículo ya está reservado en ese período.")
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

            ReservaConfirmadaEvent evento = new ReservaConfirmadaEvent(
                guardada.getId(),
                vehiculoResponse.getId(),
                guardada.getFechaHoraInicio().toString(),
                guardada.getFechaHoraFinal().toString()
            );

            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_RESERVA_CONFIRMADA, evento);

            responseObserver.onNext(CrearReservaResponse.newBuilder()
                    .setReserva(ReservaMapper.toProto(guardada))
                    .build());
            responseObserver.onCompleted();

        } catch(IllegalArgumentException | DateTimeParseException | DataIntegrityViolationException e){
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Error 400: Datos inválidos")
                    .asRuntimeException());
        } catch (io.grpc.StatusRuntimeException e) {
            responseObserver.onError(e);
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error al crear la reserva: " + e.toString())
                    .asRuntimeException());
        }
    }

    @Override
    public void obtenerReserva(ObtenerReservaRequest request, StreamObserver<ObtenerReservaResponse> responseObserver) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(request.getId());

        if (reservaOpt.isPresent()) {
            responseObserver.onNext(ObtenerReservaResponse.newBuilder()
                    .setReserva(ReservaMapper.toProto(reservaOpt.get()))
                    .build());
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
        
             if (!reserva.getFechaHoraInicio().isAfter(LocalDateTime.now())) {
                responseObserver.onError(Status.FAILED_PRECONDITION
                        .withDescription("No se puede cancelar una reserva cuyo período ya comenzó.")
                        .asRuntimeException());
                return;
            }
            reserva.setEstadoReserva(EstadoReserva.CANCELADO);
            reservaRepository.save(reserva);

            CancelarReservaResponse response = CancelarReservaResponse.newBuilder()
                    .setExito(true)
                    .setEstadoReserva(grpc.EstadoReserva.ESTADO_RESERVA_CANCELADO)
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
    public void obtenerMiHistorial(ObtenerMiHistorialRequest request, StreamObserver<ObtenerMiHistorialResponse> responseObserver) {
        try {
            List<reservas.models.Reserva> historialDb = reservaRepository.buscarHistorial(
                    request.getIdCliente(),
                    reservas.enums.EstadoReserva.CANCELADO,
                    reservas.enums.EstadoReserva.CONFIRMADO,
                    LocalDateTime.now()
            );

            ObtenerMiHistorialResponse.Builder responseBuilder = ObtenerMiHistorialResponse.newBuilder();

            for (reservas.models.Reserva cruda : historialDb) {
                long dias = Math.max(1, ChronoUnit.DAYS.between(cruda.getFechaHoraInicio(), cruda.getFechaHoraFinal()));
                EstadoHistorial estado = cruda.getEstadoReserva() == reservas.enums.EstadoReserva.CANCELADO
                        ? EstadoHistorial.ESTADO_HISTORIAL_CANCELADO
                        : EstadoHistorial.ESTADO_HISTORIAL_FINALIZADO;

                HistorialItem item = HistorialItem.newBuilder()
                        .setVehiculo("Vehículo " + cruda.getPatenteVehiculo())
                        .setPatente(cruda.getPatenteVehiculo())
                        .setFechaHoraInicio(cruda.getFechaHoraInicio().toString())
                        .setFechaHoraFinal(cruda.getFechaHoraFinal().toString())
                        .setDias(Math.toIntExact(dias))
                        .setImporteTotal(cruda.getPrecioTotal())
                        .setEstado(estado)
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
    public void getVehiculosOcupados(GetVehiculosOcupadosRequest request, StreamObserver<GetVehiculosOcupadosResponse> responseObserver) {
        try {
            LocalDateTime fechaDesde = Parsers.fechaHora(request.getFechaDesde(), "fecha_desde");
            LocalDateTime fechaHasta = Parsers.fechaHora(request.getFechaHasta(), "fecha_hasta");

            List<String> patentesOcupadas = reservaRepository.findPatentesOcupadasEnRango(
                    fechaDesde, 
                    fechaHasta, 
                    reservas.enums.EstadoReserva.CANCELADO
            );

            GetVehiculosOcupadosResponse response = GetVehiculosOcupadosResponse.newBuilder()
                    .addAllPatentes(patentesOcupadas)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        }catch(IllegalArgumentException | DateTimeParseException | DataIntegrityViolationException e){
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Error 400: Datos inválidos")
                    .asRuntimeException());
        }
        catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error al obtener vehículos ocupados: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}