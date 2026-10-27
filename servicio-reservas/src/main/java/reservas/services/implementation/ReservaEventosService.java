package reservas.services.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reservas.dtos.MantenimientoResultadoEvent;
import reservas.models.Reserva;
import reservas.repositories.IReservaRepository;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservaEventosService {

    private final IReservaRepository reservaRepository;

    @Transactional
    public void registrarResultadoMantenimiento(MantenimientoResultadoEvent evento) {
        Optional<Reserva> opt = reservaRepository.findById(evento.reservaId());

        if (opt.isEmpty()) {
            log.error("[MANTENIMIENTO] Se descartó el evento: no existe la reserva ID {}", evento.reservaId());
            return; 
        }

        Reserva reserva = opt.get();
        reserva.setEstadoMantenimiento(evento.resultado()); // REALIZADO / NO_REALIZADO
        reservaRepository.save(reserva);
        log.info("[MANTENIMIENTO] Reserva ID {} -> {}", evento.reservaId(), evento.resultado());
    }
}