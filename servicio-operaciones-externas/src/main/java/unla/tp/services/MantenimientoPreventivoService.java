package unla.tp.services;

import unla.tp.config.RabbitMQConfig;
import unla.tp.dtos.MantenimientoResultadoEvent;
import unla.tp.dtos.ReservaConfirmadaEvent;
import unla.tp.entities.SolicitudMantenimiento;
import unla.tp.enums.ResultadoMantenimiento;
import unla.tp.repositories.ISolicitudMantenimientoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MantenimientoPreventivoService {

    private final ISolicitudMantenimientoRepository mantenimientoRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public void gestionarMantenimientoReserva(ReservaConfirmadaEvent evento) {
        // Control de idempotencia por si RabbitMQ reentrega el mensaje
        if (mantenimientoRepository.existsByReservaId(evento.reservaId())) {
            log.warn("[MANTENIMIENTO] La reserva {} ya fue procesada anteriormente.", evento.reservaId());
            return;
        }

        // Regla de negocio simulada: por ejemplo, si el vehiculoId es válido se realiza;
        // podés simular fallo (NO_REALIZADO) para un vehiculoId específico de prueba (ej. ID 999)
        boolean exitoMantenimiento = evento.vehiculoId() != null && evento.vehiculoId() != 999L;

        ResultadoMantenimiento estadoFinal = exitoMantenimiento 
                ? ResultadoMantenimiento.REALIZADO 
                : ResultadoMantenimiento.NO_REALIZADO;

        // Paso 4 y 5: Generación, registro y procesamiento del mantenimiento preventivo
        SolicitudMantenimiento solicitud = SolicitudMantenimiento.builder()
                .reservaId(evento.reservaId())
                .vehiculoId(evento.vehiculoId())
                .fechaInicioReserva(evento.fechaInicio())
                .fechaFinReserva(evento.fechaFin())
                .fechaProcesamiento(LocalDateTime.now())
                .resultado(estadoFinal)
                .observaciones(exitoMantenimiento 
                        ? "Chequeo de fluidos, frenos y neumáticos completado." 
                        : "Taller sin disponibilidad técnica para el vehículo solicitado.")
                .build();

        mantenimientoRepository.save(solicitud);
        log.info("[MANTENIMIENTO] Reserva ID: {} | Vehiculo ID: {} -> {}", 
                evento.reservaId(), evento.vehiculoId(), estadoFinal);

        // Paso 6: Publicación del evento indicando el resultado
        MantenimientoResultadoEvent respuesta = new MantenimientoResultadoEvent(
                evento.reservaId(),
                evento.vehiculoId(),
                estadoFinal.name()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.RK_MANTENIMIENTO_RESULTADO,
                respuesta
        );
    }
}