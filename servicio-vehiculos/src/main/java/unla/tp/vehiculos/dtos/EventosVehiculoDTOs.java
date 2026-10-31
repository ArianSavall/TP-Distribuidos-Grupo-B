package unla.tp.vehiculos.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class EventosVehiculoDTOs {

    // --- FEATURE 1: Verificación de vehículos ---
    public record VerificacionSolicitadaEvent(Long vehiculoId, String patente) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VerificacionResultadoEvent(Long vehiculoId, String patente, String resultado) {}

    // --- FEATURE 2: Incorporación de vehículos de propietarios externos ---
    public record VehiculoExternoRegistradoEvent(Long vehiculoExternoId, Long clienteId, String patente) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ValidacionPatenteExternaEvent(Long vehiculoExternoId, String patente, String resultado) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ValidacionTitularidadExternaEvent(
            Long vehiculoExternoId,
            Long clienteId,
            String documento,
            String patente,
            String resultado
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ValidacionExternaCompletadaEvent(Long vehiculoExternoId) {}
}