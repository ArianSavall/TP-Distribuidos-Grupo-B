package reservas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import reservas.models.Reserva;
import reservas.enums.EstadoReserva;
import java.time.LocalDateTime;
import java.util.List;
@Repository("reservaRepository")
public interface IReservaRepository extends JpaRepository<Reserva, Long> {

    @Query("""
            SELECT COUNT(r) FROM Reserva r 
            WHERE r.patenteVehiculo = :patente 
              AND r.estadoReserva = :estado 
              AND (r.fechaHoraInicio < :fechaFin AND r.fechaHoraFinal > :fechaInicio)
            """)
    long countReservasSolapadas(@Param("patente") String patente,
                                @Param("estado") EstadoReserva estado,
                                @Param("fechaInicio") LocalDateTime fechaInicio,
                                @Param("fechaFin") LocalDateTime fechaFin);

    @Query("""
            SELECT r FROM Reserva r
            WHERE (:idCliente IS NULL OR r.idCliente = :idCliente)
              AND (:patenteVehiculo IS NULL OR r.patenteVehiculo = :patenteVehiculo)
              AND (:estado IS NULL OR r.estadoReserva = :estado)
              AND (:fechaDesde IS NULL OR r.fechaHoraFinal >= :fechaDesde)
              AND (:fechaHasta IS NULL OR r.fechaHoraInicio <= :fechaHasta)
            ORDER BY r.fechaHoraInicio DESC
            """)
    List<Reserva> buscarReservas(@Param("idCliente") Long idCliente,
                                @Param("patenteVehiculo") String patenteVehiculo,
                                 @Param("estado") EstadoReserva estado,
                                 @Param("fechaDesde") LocalDateTime fechaDesde,
                                 @Param("fechaHasta") LocalDateTime fechaHasta);
    @Query("""
            SELECT r FROM Reserva r
            WHERE r.idCliente = :idCliente
              AND (
                r.estadoReserva = :cancelado
                OR (
                  r.estadoReserva = :confirmado
                  AND r.fechaHoraFinal <= :ahora
                )
              )
            ORDER BY r.fechaHoraInicio DESC, r.id DESC
            """)
    List<Reserva> buscarHistorial(
            @Param("idCliente") Long idCliente,
            @Param("cancelado") EstadoReserva cancelado,
            @Param("confirmado") EstadoReserva confirmado,
            @Param("ahora") LocalDateTime ahora);

    @Query("""
            SELECT r.patenteVehiculo FROM Reserva r 
            WHERE r.estadoReserva != :estadoCancelado 
              AND (r.fechaHoraInicio < :fechaHasta AND r.fechaHoraFinal > :fechaDesde)
            """)
    List<String> findPatentesOcupadasEnRango(
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            @Param("estadoCancelado") EstadoReserva estadoCancelado);
}

