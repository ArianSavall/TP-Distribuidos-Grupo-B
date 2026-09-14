package unla.tp.tp_distribuidos.controllers.api_rest.v1;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import unla.tp.tp_distribuidos.dtos.VehiculoDTO;
import unla.tp.tp_distribuidos.enums.EstadoVehiculo;
import unla.tp.tp_distribuidos.models.Vehiculo;
import unla.tp.tp_distribuidos.services.IVehiculoService;

@RestController
@RequestMapping("/api_rest/v1/vehiculos")
public class VehiculoRestController {

	private final IVehiculoService vehiculoService;

	public VehiculoRestController(IVehiculoService vehiculoService) {
		this.vehiculoService = vehiculoService;
	}

	@GetMapping
	public ResponseEntity<List<VehiculoDTO>> getAllVehiculos() {
		List<VehiculoDTO> vehiculos = vehiculoService.findAll()
				.stream()
				.map(this::toDto)
				.toList();

		return ResponseEntity.ok(vehiculos);
	}

	@PostMapping
	public ResponseEntity<VehiculoDTO> createVehiculo(@RequestBody VehiculoDTO vehiculoDTO) {
		Vehiculo vehiculo = toEntity(vehiculoDTO);
		vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
		vehiculo.setId(null);

		Vehiculo vehiculoGuardado = vehiculoService.save(vehiculo);
		return ResponseEntity.ok(toDto(vehiculoGuardado));
	}

	private VehiculoDTO toDto(Vehiculo vehiculo) {
		return new VehiculoDTO(
				vehiculo.getPatente(),
				vehiculo.getMarca(),
				vehiculo.getModelo(),
				vehiculo.getAnio(),
				vehiculo.getColor(),
				vehiculo.getTipoVehiculo(),
				vehiculo.getPrecioDiario(),
				vehiculo.getEstado(),
				vehiculo.isEstaActivo()
		);
	}

	private Vehiculo toEntity(VehiculoDTO vehiculoDTO) {
		Vehiculo vehiculo = new Vehiculo();
		vehiculo.setPatente(vehiculoDTO.getPatente());
		vehiculo.setMarca(vehiculoDTO.getMarca());
		vehiculo.setModelo(vehiculoDTO.getModelo());
		vehiculo.setAnio(vehiculoDTO.getAnio());
		vehiculo.setColor(vehiculoDTO.getColor());
		vehiculo.setTipoVehiculo(vehiculoDTO.getTipoVehiculo());
		vehiculo.setPrecioDiario(vehiculoDTO.getPrecioDiario());
		vehiculo.setEstaActivo(vehiculoDTO.isEstaActivo());
		return vehiculo;
	}
}
