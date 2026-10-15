package unla.tp.tp_distribuidos.controllers.api_rest.v1;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unla.tp.tp_distribuidos.dtos.ReservaDTO;
import unla.tp.tp_distribuidos.services.IReservaService;

@RestController 
@RequestMapping("/api/v1/reservas") 
public class ReservaRestController {

    private final IReservaService reservaService;

    public ReservaRestController(IReservaService reservaService) {
        this.reservaService = reservaService;
    }


    @PostMapping 
    public ResponseEntity<?> altaReserva(@RequestBody ReservaDTO request) {
        try {
            return new ResponseEntity<>(reservaService.createReserva(request), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarReserva(@PathVariable("id") Long idReserva) {
        try {
            reservaService.cancelarReserva(idReserva);
            return new ResponseEntity<>("Reserva cancelada exitosamente.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}