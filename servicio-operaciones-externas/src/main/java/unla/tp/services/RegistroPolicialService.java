package unla.tp.services;

import unla.tp.config.RabbitMQConfig;
import unla.tp.dtos.*;
import unla.tp.enums.ResultadoVerificacion;
import unla.tp.repositories.IDenunciaPolicialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistroPolicialService {

    private final IDenunciaPolicialRepository denunciaRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional(readOnly = true)
    public void verificarVehiculoInterno(VerificacionSolicitadaEvent evento) {
        String patenteNormalizada = evento.patente().trim().toUpperCase();
        boolean tieneDenuncia = denunciaRepository.existsByPatenteIgnoreCaseAndActivaTrue(patenteNormalizada);

        // Para vehículos internos el resultado debe ser VALIDADO o ROBADO
        String resultado = tieneDenuncia 
                ? ResultadoVerificacion.ROBADO.name() 
                : ResultadoVerificacion.VALIDADO.name();

        log.info("[POLICIAL-INTERNO] Vehiculo ID: {} | Patente: {} -> Resultado: {}", 
                evento.vehiculoId(), patenteNormalizada, resultado);

        VerificacionResultadoEvent respuesta = new VerificacionResultadoEvent(
                evento.vehiculoId(),
                evento.patente(),
                resultado
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.RK_VERIFICACION_RESULTADO,
                respuesta
        );
    }

    @Transactional(readOnly = true)
    public void verificarVehiculoExterno(VehiculoExternoRegistradoEvent evento) {
        String patenteNormalizada = evento.patente().trim().toUpperCase();
        boolean tieneDenuncia = denunciaRepository.existsByPatenteIgnoreCaseAndActivaTrue(patenteNormalizada);

        // Para vehículos externos el resultado debe ser VALIDADO o RECHAZADO
        String resultado = tieneDenuncia 
                ? ResultadoVerificacion.RECHAZADO.name() 
                : ResultadoVerificacion.VALIDADO.name();

        log.info("[POLICIAL-EXTERNO] Solicitud Externa ID: {} | Patente: {} -> Resultado: {}", 
                evento.vehiculoExternoId(), patenteNormalizada, resultado);

        ValidacionPatenteExternaEvent respuesta = new ValidacionPatenteExternaEvent(
                evento.vehiculoExternoId(),
                evento.patente(),
                resultado
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.RK_EXTERNOS_VALIDACION_PATENTE,
                respuesta
        );
    }
}