package unla.tp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unla.tp.entities.DenunciaPolicial;

@Repository
public interface IDenunciaPolicialRepository extends JpaRepository<DenunciaPolicial, Long> {
    boolean existsByPatenteIgnoreCaseAndActivaTrue(String patente);
}
