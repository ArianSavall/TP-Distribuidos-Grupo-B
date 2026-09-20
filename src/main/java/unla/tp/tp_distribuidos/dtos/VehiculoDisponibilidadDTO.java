package unla.tp.tp_distribuidos.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VehiculoDisponibilidadDTO {
    private String patente;
    private String marca;
    private String modelo;
    private Integer anio;
    private String color;
    private String tipoVehiculo;
    private Double precioDiario;
}
