package unla.tp.vehiculos.entities;

import unla.tp.vehiculos.enums.EstadoVehiculo;
import unla.tp.vehiculos.enums.ResultadoVerificacion;
import unla.tp.vehiculos.enums.TipoVehiculo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vehiculos")
public class Vehiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String patente;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String modelo;

    @Column(nullable = false)
    private int anio;

    @Column(nullable = true)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoVehiculo tipoVehiculo;

    @Column(nullable = false)
    private double precioDiario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoVehiculo estado;

    @Column(nullable = false)
    private boolean estaActivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultadoVerificacion resultadoVerificacion = ResultadoVerificacion.PENDIENTE;

    @Column(nullable = false)
    private boolean habilitado = false;

    // --- Campos para Vehículos Externos y Validaciones ---
    private Long clienteId; // null si es de la flota propia de Rentar; con ID si es de propietario externo

    private String validacionTitularidad; // null (F1) | PENDIENTE, VALIDADO, RECHAZADO (F2)

    @Override
    public String toString() {
        return "Vehiculo{" +
                "id=" + id +
                ", patente='" + patente + '\'' +
                ", marca='" + marca + '\'' +
                ", modelo='" + modelo + '\'' +
                ", anio=" + anio +
                ", color='" + color + '\'' +
                ", tipoVehiculo=" + tipoVehiculo +
                ", precioDiario=" + precioDiario +
                ", estado=" + estado +
                ", estaActivo=" + estaActivo +
                '}';
    }
}