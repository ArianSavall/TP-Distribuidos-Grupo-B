package unla.tp.vehiculos.listeners;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import unla.tp.vehiculos.config.RabbitMQConfig;
import unla.tp.vehiculos.dtos.EventosVehiculoDTOs.*;
import unla.tp.vehiculos.services.VehiculoEventosService;

@Component
@RequiredArgsConstructor
public class VehiculoRabbitListener {

    private final VehiculoEventosService eventosService;

    @RabbitListener(queues = RabbitMQConfig.Q_VERIFICACION_RESULTADO)
    public void onVerificacionResultado(VerificacionResultadoEvent evento) {
        eventosService.actualizarVerificacionVehiculo(evento);
    }

    @RabbitListener(queues = RabbitMQConfig.Q_EXTERNOS_PATENTE)
    public void onValidacionPatenteExterna(ValidacionPatenteExternaEvent evento) {
        eventosService.procesarValidacionPatenteExterna(evento);
    }

    @RabbitListener(queues = RabbitMQConfig.Q_EXTERNOS_TITULARIDAD)
    public void onValidacionTitularidadExterna(ValidacionTitularidadExternaEvent evento) {
        eventosService.procesarValidacionTitularidadExterna(evento);
    }

    @RabbitListener(queues = RabbitMQConfig.Q_EXTERNOS_COMPLETADA)
    public void onValidacionExternaCompletada(ValidacionExternaCompletadaEvent evento) {
        eventosService.completarIncorporacionVehiculoExterno(evento);
    }
}