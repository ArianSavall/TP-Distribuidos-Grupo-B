package unla.tp.listeners;

import unla.tp.config.RabbitMQConfig;
import unla.tp.dtos.ReservaConfirmadaEvent;
import unla.tp.services.MantenimientoPreventivoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MantenimientoListener {

    private final MantenimientoPreventivoService mantenimientoService;

        @Value("${mantenimiento.delay-ms:5000}")
        private long delayMs;

    @RabbitListener(queues = RabbitMQConfig.Q_MANTENIMIENTO)
    public void onReservaConfirmada(ReservaConfirmadaEvent evento) throws InterruptedException {
        log.info("[MANTENIMIENTO] Evento recibido para reserva {}. Procesando durante {} ms...",
                evento.reservaId(), delayMs);
        Thread.sleep(delayMs);
        mantenimientoService.gestionarMantenimientoReserva(evento);
    }
}