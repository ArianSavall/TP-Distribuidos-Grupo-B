package unla.tp.tp_distribuidos.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservaDTO {
    private Long idCliente;
    private Long idVehiculo;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFinal;
}