package services;

import models.Vehiculo;

import java.util.List;
import java.util.Optional;

public interface IVehiculoService {
    Optional<Vehiculo> findById(int id);
    Optional<Vehiculo> findByPatente(String patente);
    List<Vehiculo> findAll();
    Vehiculo save(Vehiculo vehiculo);
}
