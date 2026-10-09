package models;

import enums.EstadoVehiculo;
import enums.TipoVehiculo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vehiculo {
    private Long id;
    private String patente;
    private String marca;
    private String modelo;
    private int anio;
    private String color;
    private TipoVehiculo tipoVehiculo;
    private double precioDiario;
    private EstadoVehiculo estado;
    private boolean estaActivo;
}