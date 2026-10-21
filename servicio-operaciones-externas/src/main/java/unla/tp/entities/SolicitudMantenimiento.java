package unla.tp.entities;

import unla.tp.enums.ResultadoMantenimiento;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_mantenimiento")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SolicitudMantenimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reservaId;

    @Column(nullable = false)
    private Long vehiculoId;

    @Column(nullable = false)
    private String fechaInicioReserva;

    @Column(nullable = false)
    private String fechaFinReserva;

    @Column(nullable = false)
    private LocalDateTime fechaProcesamiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultadoMantenimiento resultado;

    private String observaciones;
}