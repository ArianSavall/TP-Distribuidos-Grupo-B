package unla.tp.tp_distribuidos.services.implementation;

import org.springframework.stereotype.Service;
import unla.tp.tp_distribuidos.models.Vehiculo;
import unla.tp.tp_distribuidos.repositories.IVehiculoRepository;
import unla.tp.tp_distribuidos.services.IVehiculoService;

import java.util.List;
import java.util.Optional;

@Service("vehiculoService")
public class VehiculoService implements IVehiculoService {
    private final IVehiculoRepository vehiculoRepository;

    public VehiculoService(IVehiculoRepository vehiculoRepository) {
        this.vehiculoRepository = vehiculoRepository;
    }


    @Override
    public Optional<Vehiculo> findById(int id) {
        return vehiculoRepository.findById(id);
    }

    @Override
    public List<Vehiculo> findAll() {
        return vehiculoRepository.findAll();
    }

    @Override
    public Vehiculo save(Vehiculo vehiculo) {
        return vehiculoRepository.save(vehiculo);
    }
}
