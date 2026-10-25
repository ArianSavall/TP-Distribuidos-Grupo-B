package unla.tp.listeners;

import unla.tp.config.RabbitMQConfig;
import unla.tp.dtos.ReservaConfirmadaEvent;
import unla.tp.services.MantenimientoPreventivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MantenimientoListener {

    private final MantenimientoPreventivoService mantenimientoService;

    @RabbitListener(queues = RabbitMQConfig.Q_MANTENIMIENTO)
    public void onReservaConfirmada(ReservaConfirmadaEvent evento) {
        mantenimientoService.gestionarMantenimientoReserva(evento);
    }
}