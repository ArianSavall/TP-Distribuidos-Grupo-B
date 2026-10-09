package unla.tp.tp_distribuidos.reservas.models;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import unla.tp.tp_distribuidos.reservas.enums.EstadoReserva;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {
    private Long idReserva;
    private String patenteVehiculo; 
    private Long idCliente;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFinal;
    private EstadoReserva estadoReserva;
    private double importeTotal;
}