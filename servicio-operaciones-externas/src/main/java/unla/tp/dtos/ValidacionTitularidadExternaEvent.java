package unla.tp.dtos;

public record ValidacionTitularidadExternaEvent(
        Long vehiculoExternoId,
        Long clienteId,
        String documento,
        String patente,
        String resultado
) {}

