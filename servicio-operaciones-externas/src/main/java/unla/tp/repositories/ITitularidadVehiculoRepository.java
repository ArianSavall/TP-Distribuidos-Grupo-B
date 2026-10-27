package unla.tp.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import unla.tp.entities.TitularidadVehiculo;

@Repository("TitularidadVehiculoRepository")
public interface ITitularidadVehiculoRepository extends JpaRepository<TitularidadVehiculo, Long> {

    Optional<TitularidadVehiculo> findByPatenteIgnoreCase(String patente);
    
}
