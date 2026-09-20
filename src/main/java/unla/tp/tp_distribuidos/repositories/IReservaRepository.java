package unla.tp.tp_distribuidos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import unla.tp.tp_distribuidos.models.Reserva;
import unla.tp.tp_distribuidos.enums.EstadoReserva;
import unla.tp.tp_distribuidos.enums.TipoVehiculo;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IReservaRepository extends JpaRepository<Reserva, Long> {

    @Query("""
            SELECT COUNT(r) FROM Reserva r 
            WHERE r.vehiculo.id = :vehiculoId 
              AND r.estadoReserva = :estado 
              AND (r.fechaHoraInicio < :fechaFin AND r.fechaHoraFinal > :fechaInicio)
            """)
    long countReservasSolapadas(@Param("vehiculoId") Long vehiculoId,
                                @Param("estado") EstadoReserva estado,
                                @Param("fechaInicio") LocalDateTime fechaInicio,
                                @Param("fechaFin") LocalDateTime fechaFin);

    @Query("""
            SELECT r FROM Reserva r
            JOIN FETCH r.cliente c
            JOIN FETCH r.vehiculo v
            WHERE (:dniCliente IS NULL OR c.dni = :dniCliente)
              AND (:clienteAutenticadoId IS NULL OR c.id = :clienteAutenticadoId)
              AND (:patenteVehiculo IS NULL OR v.patente = :patenteVehiculo)
              AND (:tipoVehiculo IS NULL OR v.tipoVehiculo = :tipoVehiculo)
              AND (:estado IS NULL OR r.estadoReserva = :estado)
              AND (:fechaDesde IS NULL OR r.fechaHoraFinal >= :fechaDesde)
              AND (:fechaHasta IS NULL OR r.fechaHoraInicio <= :fechaHasta)
            ORDER BY r.fechaHoraInicio DESC
            """)
    List<Reserva> buscarReservas(@Param("dniCliente") String dniCliente,
                                 @Param("clienteAutenticadoId") Long clienteAutenticadoId,
                   @Param("patenteVehiculo") String patenteVehiculo,
                                 @Param("tipoVehiculo") TipoVehiculo tipoVehiculo,
                                 @Param("estado") EstadoReserva estado,
                                 @Param("fechaDesde") LocalDateTime fechaDesde,
                                 @Param("fechaHasta") LocalDateTime fechaHasta);
    @Query("""
            SELECT r FROM Reserva r
            JOIN FETCH r.vehiculo
            WHERE r.cliente.metadatos.usuario = :usuario
              AND (
                r.estadoReserva = :cancelado
                OR (
                  r.estadoReserva = :confirmado
                  AND r.fechaHoraFinal <= :ahora
                )
              )
            ORDER BY r.fechaHoraInicio DESC, r.idReserva DESC
            """)
    List<Reserva> buscarHistorial(
            @Param("usuario") String usuario,
            @Param("cancelado") EstadoReserva cancelado,
            @Param("confirmado") EstadoReserva confirmado,
            @Param("ahora") LocalDateTime ahora);

}

