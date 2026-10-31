package reservas.listeners;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import reservas.config.RabbitMQConfig;
import reservas.dtos.MantenimientoResultadoEvent;
import reservas.services.implementation.ReservaEventosService;

@Component
@RequiredArgsConstructor
public class MantenimientoListener {

    private final ReservaEventosService reservaEventosService;

    @RabbitListener(queues = RabbitMQConfig.Q_MANTENIMIENTO_RESULTADO)
    public void onMantenimientoResultado(MantenimientoResultadoEvent evento) {
        reservaEventosService.registrarResultadoMantenimiento(evento);
    }
}