package unla.tp.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Entrada Feature 3: reserva.confirmada
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReservaConfirmadaEvent(Long reservaId, Long vehiculoId, String fechaInicio, String fechaFin) {}
