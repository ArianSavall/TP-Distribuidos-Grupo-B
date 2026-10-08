package unla.tp.tp_distribuidos.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import unla.tp.tp_distribuidos.enums.EstadoReserva;
import unla.tp.tp_distribuidos.enums.TipoVehiculo;
import unla.tp.tp_distribuidos.models.Vehiculo;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    @Query("""
        SELECT v
        FROM Vehiculo v
        WHERE v.estaActivo = true

        AND (:tipo IS NULL OR v.tipoVehiculo = :tipo)
        AND (:marca IS NULL OR LOWER(v.marca) = LOWER(:marca))
        AND (:modelo IS NULL OR LOWER(v.modelo) = LOWER(:modelo))
        AND (:precioMin IS NULL OR v.precioDiario >= :precioMin)
        AND (:precioMax IS NULL OR v.precioDiario <= :precioMax)

        AND NOT EXISTS (
            SELECT r.idReserva
            FROM Reserva r
            WHERE r.vehiculo = v
            AND r.estadoReserva = :estadoConfirmado
            AND r.fechaHoraInicio < :fechaFinal
            AND r.fechaHoraFinal > :fechaInicio
        )
    """)
    List<Vehiculo> buscarDisponibles(
        @Param("tipo") TipoVehiculo tipo,
        @Param("marca") String marca,
        @Param("modelo") String modelo,
        @Param("precioMin") Double precioMin,
        @Param("precioMax") Double precioMax,
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFinal") LocalDateTime fechaFinal,
        @Param("estadoConfirmado") EstadoReserva estadoConfirmado
    );
}