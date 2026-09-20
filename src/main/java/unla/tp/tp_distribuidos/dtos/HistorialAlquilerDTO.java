package unla.tp.tp_distribuidos.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HistorialAlquilerDTO {

    private String vehiculo;
    private String patente;
    private String fechaInicio;
    private String fechaFinalizacion;
    private int cantidadDias;
    private double importeTotal;
    private String estado;
}