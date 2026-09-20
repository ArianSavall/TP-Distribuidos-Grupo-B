package unla.tp.tp_distribuidos.controllers.api_graphql;

import java.util.List;

import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import unla.tp.tp_distribuidos.dtos.HistorialAlquilerDTO;
import unla.tp.tp_distribuidos.services.implementation.HistorialAlquilerService;

@Controller
public class HistorialAlquilerController {

    private final HistorialAlquilerService historialService;

    public HistorialAlquilerController(
            HistorialAlquilerService historialService) {
        this.historialService = historialService;
    }

    @QueryMapping
    public List<HistorialAlquilerDTO> miHistorialAlquileres() {
        return historialService.obtenerMiHistorial();
    }
}