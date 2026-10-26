package unla.tp.vehiculos.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unla.tp.vehiculos.config.RabbitMQConfig;
import unla.tp.vehiculos.dtos.EventosVehiculoDTOs.*;
import unla.tp.vehiculos.entities.Vehiculo;
import unla.tp.vehiculos.enums.EstadoVehiculo;
import unla.tp.vehiculos.enums.ResultadoVerificacion;
import unla.tp.vehiculos.repositories.IVehiculoRepository;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehiculoEventosService {

    private final IVehiculoRepository vehiculoRepository;
    private final RabbitTemplate rabbitTemplate;

    // --- FEATURE 1: Verificación de vehículo propio ---
    @Transactional
    public void actualizarVerificacionVehiculo(VerificacionResultadoEvent evento) {
        Optional<Vehiculo> optVehiculo = vehiculoRepository.findByPatente(evento.patente());

        if (optVehiculo.isEmpty()) {
            log.error("[ERROR] Se descartó el evento porque no existe el vehículo con patente: {}", evento.patente());
            return; // Termina normalmente -> RabbitMQ saca el mensaje de la cola y no hace bucle
        }

        Vehiculo vehiculo = optVehiculo.get();
        vehiculo.setResultadoVerificacion(ResultadoVerificacion.valueOf(evento.resultado())); // "VALIDADO" o "ROBADO"
        // Si es ROBADO, no cambia EstadoVehiculo pero queda inhabilitado para operar
        vehiculo.setHabilitado("VALIDADO".equalsIgnoreCase(evento.resultado()));
        vehiculoRepository.save(vehiculo);
    }

    // --- FEATURE 2: Correlación sobre la misma entidad Vehiculo ---
    @Transactional
    public void procesarValidacionPatenteExterna(ValidacionPatenteExternaEvent evento) {
        Optional<Vehiculo> optVehiculo = vehiculoRepository.findByPatente(evento.patente());

        if (optVehiculo.isEmpty()) {
            log.error("[ERROR] Se descartó el evento porque no existe el vehículo con patente: {}", evento.patente());
            return; // Termina normalmente -> RabbitMQ saca el mensaje de la cola y no hace bucle
        }

        Vehiculo vehiculo = optVehiculo.get();
        vehiculo.setResultadoVerificacion(ResultadoVerificacion.valueOf(evento.resultado().toUpperCase()));
        evaluarCorrelacion(vehiculo);
    }

    @Transactional
    public void procesarValidacionTitularidadExterna(ValidacionTitularidadExternaEvent evento) {
        Optional<Vehiculo> optVehiculo = vehiculoRepository.findByPatente(evento.patente());

        if (optVehiculo.isEmpty()) {
            log.error("[ERROR] Se descartó el evento porque no existe el vehículo con patente: {}", evento.patente());
            return; // Termina normalmente -> RabbitMQ saca el mensaje de la cola y no hace bucle
        }

        Vehiculo vehiculo = optVehiculo.get();
        vehiculo.setValidacionTitularidad(evento.resultado().toUpperCase());
        evaluarCorrelacion(vehiculo);
    }

    private void evaluarCorrelacion(Vehiculo vehiculo) {
        // Esperamos hasta que ambas validaciones dejen de estar en PENDIENTE
        if ("PENDIENTE".equals(vehiculo.getResultadoVerificacion().name()) ||
                "PENDIENTE".equals(vehiculo.getValidacionTitularidad())) {
            vehiculoRepository.save(vehiculo);
            return;
        }

        boolean patenteOk = "VALIDADO".equals(vehiculo.getResultadoVerificacion().name());
        boolean titularidadOk = "VALIDADO".equals(vehiculo.getValidacionTitularidad());

        if (patenteOk && titularidadOk) {
            vehiculoRepository.save(vehiculo);
            // Publicamos el evento final de completitud
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.RK_EXTERNOS_VALIDACION_COMPLETADA,
                    new ValidacionExternaCompletadaEvent(vehiculo.getId())
            );
        } else {
            // Si alguna falla, no se incorpora (queda inhabilitado en PENDIENTE o se elimina físicamente)
            vehiculo.setHabilitado(false);
            vehiculoRepository.save(vehiculo);
            log.warn("Vehículo externo ID {} rechazado. No se incorpora a la flota activa.", vehiculo.getId());
        }
    }

    @Transactional
    public void completarIncorporacionVehiculoExterno(ValidacionExternaCompletadaEvent evento) {
        Optional<Vehiculo> optVehiculo = vehiculoRepository.findById(evento.vehiculoExternoId());

        if (optVehiculo.isEmpty()) {
            log.error("[ERROR] Se descartó el evento porque no existe el vehículo con ID: {}", evento.vehiculoExternoId());
            return; // Termina normalmente -> RabbitMQ saca el mensaje de la cola y no hace bucle
        }

        Vehiculo vehiculo = optVehiculo.get();
        vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
        vehiculo.setHabilitado(true);
        vehiculoRepository.save(vehiculo);
        log.info("Vehículo externo ID {} incorporado exitosamente como DISPONIBLE.", vehiculo.getId());
    }

    private Vehiculo buscarVehiculo(Long id) {
        return vehiculoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado con ID: " + id));
    }
}