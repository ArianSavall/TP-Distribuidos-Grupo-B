package repositories;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import entities.Vehiculo;

@Repository("VehiculoRepository")
public interface IVehiculoRepository extends JpaRepository<Vehiculo, Integer>{
    Optional<Vehiculo> findById(Long id);
    Optional<Vehiculo> findByPatente(String patente);
    List<Vehiculo> findAll();
}
