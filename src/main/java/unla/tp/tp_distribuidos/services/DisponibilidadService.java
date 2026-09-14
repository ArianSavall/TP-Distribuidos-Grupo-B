package unla.tp.tp_distribuidos.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import unla.tp.tp_distribuidos.enums.EstadoReserva;
import unla.tp.tp_distribuidos.enums.TipoVehiculo;
import unla.tp.tp_distribuidos.models.Vehiculo;
import unla.tp.tp_distribuidos.repositories.VehiculoRepository;

@Service
@RequiredArgsConstructor
public class DisponibilidadService {

    private final VehiculoRepository vehiculoRepository;

    public List<Vehiculo> buscarDisponibles(
            TipoVehiculo tipo,
            String marca,
            String modelo,
            Double precioMin,
            Double precioMax,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFinal) {

        if (fechaInicio == null || fechaFinal == null) {
            throw new IllegalArgumentException("Las fechas de inicio y finalización son obligatorias");
        }

        if (!fechaFinal.isAfter(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de finalización debe ser posterior a la fecha de inicio");
        }

        if (precioMin != null && precioMax != null && precioMin > precioMax) {
            throw new IllegalArgumentException("El precio mínimo no puede ser mayor al precio máximo");
        }
        
        return vehiculoRepository.buscarDisponibles(
            tipo,
            marca,
            modelo,
            precioMin,
            precioMax,
            fechaInicio,
            fechaFinal,
            EstadoReserva.CONFIRMADO
        );
    }
}