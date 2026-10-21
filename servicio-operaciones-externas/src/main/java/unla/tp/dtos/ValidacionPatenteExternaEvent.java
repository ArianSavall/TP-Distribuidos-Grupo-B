package unla.tp.dtos;

// Salida Feature 2: vehiculos.externos.validacion.patente
public record ValidacionPatenteExternaEvent(Long vehiculoExternoId, String patente, String resultado) {}
