package unla.tp.tp_distribuidos.controllers.api_rest.v1;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unla.tp.tp_distribuidos.dtos.PatenteDTO;
import unla.tp.tp_distribuidos.dtos.VehiculoDTO;
import unla.tp.tp_distribuidos.dtos.VehiculoUpdateDTO;
import unla.tp.tp_distribuidos.enums.EstadoVehiculo;
import unla.tp.tp_distribuidos.models.Vehiculo;
import unla.tp.tp_distribuidos.services.IVehiculoService;

import java.util.List;

@RestController
@RequestMapping("/api_rest/v1/vehiculos")
public class VehiculoRestController {

	private final IVehiculoService vehiculoService;

	public VehiculoRestController(IVehiculoService vehiculoService) {
		this.vehiculoService = vehiculoService;
	}

	@GetMapping("/getAll")
	public ResponseEntity<List<VehiculoDTO>> getAllVehiculos() {
		List<VehiculoDTO> vehiculos = vehiculoService.findAll()
				.stream()
				.map(this::toDto)
				.toList();

		return ResponseEntity.ok(vehiculos);
	}

	@PostMapping("/create")
	public ResponseEntity<VehiculoDTO> createVehiculo(@RequestBody VehiculoDTO vehiculoDTO) {
		if (vehiculoService.findByPatente(vehiculoDTO.getPatente()).isPresent()) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}

		Vehiculo vehiculo = toEntity(vehiculoDTO);
		vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
		vehiculo.setId(null);

		Vehiculo vehiculoGuardado = vehiculoService.save(vehiculo);
		return ResponseEntity.ok(toDto(vehiculoGuardado));
	}

	@PostMapping("/desactivar")
	public ResponseEntity<VehiculoDTO> desactivarVehiculo(@RequestBody PatenteDTO patenteDTO) {
		return vehiculoService.findByPatente(patenteDTO.getPatente())
				.map(vehiculo -> {
					vehiculo.setEstaActivo(false);
					Vehiculo vehiculoActualizado = vehiculoService.save(vehiculo);
					return ResponseEntity.ok(toDto(vehiculoActualizado));
				})
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping("/activar")
	public ResponseEntity<VehiculoDTO> activarVehiculo(@RequestBody PatenteDTO patenteDTO) {
		return vehiculoService.findByPatente(patenteDTO.getPatente())
				.map(vehiculo -> {
					vehiculo.setEstaActivo(true);
					Vehiculo vehiculoActualizado = vehiculoService.save(vehiculo);
					return ResponseEntity.ok(toDto(vehiculoActualizado));
				})
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping("/{patente}")
	public ResponseEntity<VehiculoDTO> getVehiculoByPatente(@PathVariable String patente) {
		return vehiculoService.findByPatente(patente)
				.map(vehiculo -> ResponseEntity.ok(toDto(vehiculo)))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PutMapping("/{patente}")
	public ResponseEntity<VehiculoDTO> updateVehiculoByPatente(@PathVariable String patente,
													   @RequestBody VehiculoUpdateDTO vehiculoDTO) {
		return vehiculoService.findByPatente(patente)
				.map(vehiculo -> {
					vehiculo.setPatente(patente);

					if (vehiculoDTO.getMarca() != null) {
						vehiculo.setMarca(vehiculoDTO.getMarca());
					}
					if (vehiculoDTO.getModelo() != null) {
						vehiculo.setModelo(vehiculoDTO.getModelo());
					}
					if (vehiculoDTO.getAnio() != null) {
						vehiculo.setAnio(vehiculoDTO.getAnio());
					}
					if (vehiculoDTO.getColor() != null) {
						vehiculo.setColor(vehiculoDTO.getColor());
					}
					if (vehiculoDTO.getTipoVehiculo() != null) {
						vehiculo.setTipoVehiculo(vehiculoDTO.getTipoVehiculo());
					}
					if (vehiculoDTO.getPrecioDiario() != null) {
						vehiculo.setPrecioDiario(vehiculoDTO.getPrecioDiario());
					}

					Vehiculo vehiculoActualizado = vehiculoService.save(vehiculo);
					return ResponseEntity.ok(toDto(vehiculoActualizado));
				})
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	private VehiculoDTO toDto(Vehiculo vehiculo) {
		return new VehiculoDTO(
				vehiculo.getId(),
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
