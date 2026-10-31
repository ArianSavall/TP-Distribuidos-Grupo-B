package unla.tp.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Entrada Feature 2: vehiculos.externos.registrados
@JsonIgnoreProperties(ignoreUnknown = true)
public record VehiculoExternoRegistradoEvent(Long vehiculoExternoId, Long clienteId, String patente) {}
