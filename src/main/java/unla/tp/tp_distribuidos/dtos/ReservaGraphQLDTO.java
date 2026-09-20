package unla.tp.tp_distribuidos.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import unla.tp.tp_distribuidos.enums.EstadoReserva;

@Getter
@AllArgsConstructor
public class ReservaGraphQLDTO {
    private String cliente;
    private String vehiculo;
    private String patente;
    private String fechaInicio;
    private String fechaFinalizacion;
    private double precioDiario;
    private double importeTotal;
    private EstadoReserva estado;
}