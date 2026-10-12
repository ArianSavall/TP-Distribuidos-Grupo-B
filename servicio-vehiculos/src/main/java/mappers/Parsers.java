package mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Conversión de strings del contrato RPC a tipos Java. Si el formato es inválido se lanza
 * IllegalArgumentException (gRPC INVALID_ARGUMENT). Es validación de formato, no de negocio.
 */
public final class Parsers {

    private Parsers() {
    }

    public static <E extends Enum<E>> E enumDe(Class<E> tipo, String valor, String campo) {
        try {
            return Enum.valueOf(tipo, valor);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Valor inválido para '" + campo + "': '" + valor + "' (" + tipo.getSimpleName() + ")");
        }
    }

    public static LocalDateTime fechaHora(String valor, String campo) {
        try {
            return LocalDateTime.parse(valor);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Valor inválido para '" + campo + "': '" + valor + "' (se espera ISO-8601, ej. 2026-10-01T10:00:00)");
        }
    }

    public static LocalDate fecha(String valor, String campo) {
        try {
            return LocalDate.parse(valor);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Valor inválido para '" + campo + "': '" + valor + "' (se espera yyyy-MM-dd)");
        }
    }
}
