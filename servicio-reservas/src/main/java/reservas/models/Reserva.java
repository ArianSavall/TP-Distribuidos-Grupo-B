package reservas.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import reservas.enums.EstadoReserva;

@Data
@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Long id;

    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    @Column(name = "patente_vehiculo", nullable = false)
    private String patenteVehiculo;

    @Column(name = "fecha_hora_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_final", nullable = false)
    private LocalDateTime fechaHoraFinal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_reserva", nullable = false)
    private EstadoReserva estadoReserva;

    @Column(name = "precio_total", nullable = false)
    private Double precioTotal;
}