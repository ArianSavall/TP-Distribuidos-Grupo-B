package unla.tp.tp_distribuidos.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import unla.tp.tp_distribuidos.enums.EstadoReserva;

@Getter
@Setter 
@NoArgsConstructor 
@AllArgsConstructor
public class ReservaGraphQLDTO {
    private Long id;
    private String cliente;
    private String vehiculo;
    private String patente;
    private String fechaInicio;
    private String fechaFinalizacion;
    private double precioDiario;
    private double importeTotal;
    private EstadoReserva estado;
}