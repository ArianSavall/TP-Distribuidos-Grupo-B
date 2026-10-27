package unla.tp.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "titularidades_vehiculos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TitularidadVehiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 15)
    private String patente;

    @Column(nullable = false)
    private Long clienteId;

    @Column(nullable = false, length = 20)
    private String documento;
}
