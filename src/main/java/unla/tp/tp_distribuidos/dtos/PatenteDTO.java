package unla.tp.tp_distribuidos.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PatenteDTO", description = "Patente usada para desactivar un vehiculo")
public class PatenteDTO {

	@Schema(description = "Patente del vehiculo", example = "AB123CD")
	private String patente;
}