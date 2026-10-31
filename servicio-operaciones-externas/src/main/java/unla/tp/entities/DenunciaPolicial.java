package unla.tp.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "denuncias_policiales")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DenunciaPolicial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 15)
    private String patente;

    @Column(nullable = false)
    private String motivo;

    @Column(nullable = false)
    private LocalDateTime fechaDenuncia;

    @Column(nullable = false)
    private boolean activa;
}