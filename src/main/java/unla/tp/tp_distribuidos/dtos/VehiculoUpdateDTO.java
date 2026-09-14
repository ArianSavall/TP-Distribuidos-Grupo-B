package unla.tp.tp_distribuidos.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import unla.tp.tp_distribuidos.enums.TipoVehiculo;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "VehiculoUpdateDTO", description = "Datos opcionales para actualizar un vehiculo por patente")
public class VehiculoUpdateDTO {

	@Schema(description = "Marca del vehiculo", example = "Toyota")
	private String marca;

	@Schema(description = "Modelo del vehiculo", example = "Corolla")
	private String modelo;

	@Schema(description = "Año del vehiculo", example = "2023")
	private Integer anio;

	@Schema(description = "Color del vehiculo", example = "Blanco")
	private String color;

	@Schema(description = "Tipo de vehiculo", example = "SEDAN", enumAsRef = true)
	private TipoVehiculo tipoVehiculo;

	@Schema(description = "Precio diario de alquiler", example = "45000.0")
	private Double precioDiario;
}