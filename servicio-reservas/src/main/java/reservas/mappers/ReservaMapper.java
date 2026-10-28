package reservas.mappers;

import grpc.EstadoMantenimiento;
import grpc.EstadoReserva;
import grpc.Reserva;

public final class ReservaMapper {
    private ReservaMapper() {}

    public static Reserva toProto(reservas.models.Reserva dbReserva) {
        EstadoReserva estadoGrpc = dbReserva.getEstadoReserva() == reservas.enums.EstadoReserva.CONFIRMADO
                ? EstadoReserva.ESTADO_RESERVA_CONFIRMADO
                : EstadoReserva.ESTADO_RESERVA_CANCELADO;

        return Reserva.newBuilder()
                .setIdReserva(dbReserva.getId())
                .setPatenteVehiculo(dbReserva.getPatenteVehiculo())
                .setIdCliente(dbReserva.getIdCliente())
                .setFechaHoraInicio(dbReserva.getFechaHoraInicio().toString())
                .setFechaHoraFinal(dbReserva.getFechaHoraFinal().toString())
                .setImporteTotal(dbReserva.getPrecioTotal())
                .setEstadoReserva(estadoGrpc)
                .setEstadoMantenimiento(mantenimientoToProto(dbReserva.getEstadoMantenimiento()))
                .build();
    }

    // En la base y en los eventos de RabbitMQ el resultado viaja como texto.
    // Un valor nulo o desconocido (reservas anteriores al Hito 3) se muestra PENDIENTE.
    private static EstadoMantenimiento mantenimientoToProto(String valor) {
        if (valor == null) {
            return EstadoMantenimiento.ESTADO_MANTENIMIENTO_PENDIENTE;
        }
        return switch (valor) {
            case "REALIZADO" -> EstadoMantenimiento.ESTADO_MANTENIMIENTO_REALIZADO;
            case "NO_REALIZADO" -> EstadoMantenimiento.ESTADO_MANTENIMIENTO_NO_REALIZADO;
            default -> EstadoMantenimiento.ESTADO_MANTENIMIENTO_PENDIENTE;
        };
    }
}