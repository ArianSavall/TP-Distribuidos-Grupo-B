package unla.tp.tp_distribuidos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import unla.tp.tp_distribuidos.models.Reserva;
import unla.tp.tp_distribuidos.enums.EstadoReserva;

import java.time.LocalDateTime;

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
}