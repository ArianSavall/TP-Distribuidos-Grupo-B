package unla.tp.dtos;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Entrada Feature 1: vehiculos.verificacion.solicitada
@JsonIgnoreProperties(ignoreUnknown = true)
public record VerificacionSolicitadaEvent(Long vehiculoId, String patente) {}

