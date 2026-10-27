package unla.tp.vehiculos.repositories;

import unla.tp.vehiculos.entities.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

@Repository("VehiculoRepository")
public interface IVehiculoRepository extends JpaRepository<Vehiculo, Integer>{
    Optional<Vehiculo> findById(Long id);
    Optional<Vehiculo> findByPatente(String patente);
    List<Vehiculo> findAll();

    // Si la lista de ocupados tiene elementos, exclúyelos
    @Query("SELECT v FROM Vehiculo v WHERE v.estaActivo = true AND v.patente NOT IN :patentesOcupadas")
    List<Vehiculo> findDisponiblesExcluyendo(@Param("patentesOcupadas") List<String> patentesOcupadas);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Vehiculo v WHERE v.id = :id")
    Optional<Vehiculo> findByIdForUpdate(@Param("id") Long id);
}