package unla.tp.tp_distribuidos.services.implementation;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import unla.tp.tp_distribuidos.dtos.HistorialAlquilerDTO;
import unla.tp.tp_distribuidos.enums.EstadoReserva;
import unla.tp.tp_distribuidos.models.Reserva;
import unla.tp.tp_distribuidos.repositories.IReservaRepository;

@Service
public class HistorialAlquilerService {

    private final IReservaRepository reservaRepository;

    public HistorialAlquilerService(IReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    @PreAuthorize("hasRole('CLIENTE')")
    @Transactional(readOnly = true)
    public List<HistorialAlquilerDTO> obtenerMiHistorial() {
        Authentication autenticacion =
                SecurityContextHolder.getContext().getAuthentication();

        return reservaRepository.buscarHistorial(
                autenticacion.getName(),
                EstadoReserva.CANCELADO,
                EstadoReserva.CONFIRMADO,
                LocalDateTime.now()
        ).stream().map(this::convertir).toList();
    }

    private HistorialAlquilerDTO convertir(Reserva reserva) {
        // Mismo criterio de dias que utiliza el alta de reservas.
        long dias = Math.max(1, ChronoUnit.DAYS.between(
                reserva.getFechaHoraInicio(),
                reserva.getFechaHoraFinal()
        ));

        // FINALIZADO se muestra en el historial, sin cambiar la reserva.
        String estado = reserva.getEstadoReserva() == EstadoReserva.CANCELADO
                ? "CANCELADO"
                : "FINALIZADO";

        return new HistorialAlquilerDTO(
                reserva.getVehiculo().getMarca() + " "
                        + reserva.getVehiculo().getModelo(),
                reserva.getVehiculo().getPatente(),
                reserva.getFechaHoraInicio().toString(),
                reserva.getFechaHoraFinal().toString(),
                Math.toIntExact(dias),
                reserva.getImporteTotal(),
                estado
        );
    }
}