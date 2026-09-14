package unla.tp.tp_distribuidos.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import unla.tp.tp_distribuidos.enums.EstadoReserva;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservaResponseDTO {
    private Long idReserva;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFinal;
    private double importeTotal;
    private EstadoReserva estadoReserva;
}