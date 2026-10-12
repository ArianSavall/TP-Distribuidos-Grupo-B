package reservas.mappers;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public final class Parsers {
    private Parsers() {}

    public static LocalDateTime fechaHora(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("ERROR: " + campo + " es obligatorio.");
        }
        try {
            return LocalDateTime.parse(valor);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Valor inválido para '" + campo + "': '" + valor + "' (se espera ej. 2026-10-01T10:00:00)");
        }
    }
}