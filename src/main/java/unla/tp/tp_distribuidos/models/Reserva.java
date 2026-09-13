package unla.tp.tp_distribuidos.models;

import java.time.LocalDateTime;

public class Reserva {
    private long idReserva;
    private Vehiculo vehiculo;
    private Usuario cliente;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFinal;
    private EstadoReserva estadoReserva;
    private double importeTotal;
}
