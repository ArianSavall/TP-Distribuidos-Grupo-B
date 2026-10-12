package reservas.mappers;

import grpc.Reserva;
import grpc.EstadoReserva;

public final class ReservaMapper {
    private ReservaMapper() {}

    public static Reserva toProto(reservas.models.Reserva dbReserva) {
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
}