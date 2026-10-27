package reservas.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Entrada: mantenimiento.resultado
@JsonIgnoreProperties(ignoreUnknown = true)
public record MantenimientoResultadoEvent(Long reservaId, Long vehiculoId, String resultado) {}