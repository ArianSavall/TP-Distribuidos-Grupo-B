package unla.tp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unla.tp.entities.SolicitudMantenimiento;

@Repository("SolicitudMantenimientoRepository")
public interface ISolicitudMantenimientoRepository extends JpaRepository<SolicitudMantenimiento, Long> {
    boolean existsByReservaId(Long reservaId);
}
