package unla.tp.listeners;

import unla.tp.config.RabbitMQConfig;
import unla.tp.dtos.VehiculoExternoRegistradoEvent;
import unla.tp.dtos.VerificacionSolicitadaEvent;
import unla.tp.services.RegistroPolicialService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegistroPolicialListener {

    private final RegistroPolicialService policialService;

    @RabbitListener(queues = RabbitMQConfig.Q_VERIFICACION_INTERNA)
    public void onVerificacionSolicitada(VerificacionSolicitadaEvent evento) {
        policialService.verificarVehiculoInterno(evento);
    }

    @RabbitListener(queues = RabbitMQConfig.Q_VERIFICACION_EXTERNA)
    public void onVehiculoExternoRegistrado(VehiculoExternoRegistradoEvent evento) {
        policialService.verificarVehiculoExterno(evento);
    }
}