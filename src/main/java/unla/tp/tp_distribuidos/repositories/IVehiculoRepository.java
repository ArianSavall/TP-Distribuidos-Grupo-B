package unla.tp.tp_distribuidos.repositories;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unla.tp.tp_distribuidos.models.Vehiculo;

@Repository("VehiculoRepository")
public interface IVehiculoRepository extends JpaRepository<Vehiculo, Integer>{
    Optional<Vehiculo> findById(int id);
    Optional<Vehiculo> findByPatente(String patente);
    List<Vehiculo> findAll();
}
