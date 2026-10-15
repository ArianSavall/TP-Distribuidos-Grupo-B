<<<<<<<< HEAD:servicio-reservas/src/main/java/reservas/repositories/IReservaRepository.java
package reservas.repositories;
========
package unla.tp.tp_distribuidos.repositories;
>>>>>>>> master:src/main/java/unla/tp/tp_distribuidos/repositories/IReservaRepository.java

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

<<<<<<<< HEAD:servicio-reservas/src/main/java/reservas/repositories/IReservaRepository.java
import reservas.models.Reserva;
import reservas.enums.EstadoReserva;
========
import unla.tp.tp_distribuidos.models.Reserva;
import unla.tp.tp_distribuidos.enums.EstadoReserva;
import unla.tp.tp_distribuidos.enums.TipoVehiculo;
>>>>>>>> master:src/main/java/unla/tp/tp_distribuidos/repositories/IReservaRepository.java

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
<<<<<<<< HEAD:servicio-reservas/src/main/java/reservas/repositories/IReservaRepository.java
    List<Reserva> buscarHistorial(
            @Param("idCliente") Long idCliente,
========
    java.util.List<Reserva> buscarHistorial(
            @Param("usuario") String usuario,
>>>>>>>> master:src/main/java/unla/tp/tp_distribuidos/repositories/IReservaRepository.java
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

