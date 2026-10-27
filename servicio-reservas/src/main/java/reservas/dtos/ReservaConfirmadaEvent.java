package reservas.dtos;

public record ReservaConfirmadaEvent(
    Long reservaId,
    Long vehiculoId,
    String fechaInicio,
    String fechaFin
) {}