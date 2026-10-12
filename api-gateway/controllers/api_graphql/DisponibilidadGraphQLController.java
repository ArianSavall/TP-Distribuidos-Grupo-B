package unla.tp.tp_distribuidos.controllers.api_graphql;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import unla.tp.tp_distribuidos.enums.TipoVehiculo;
import unla.tp.tp_distribuidos.models.Vehiculo;
import unla.tp.tp_distribuidos.services.DisponibilidadService;

@Controller
@RequiredArgsConstructor
public class DisponibilidadGraphQLController {

    private final DisponibilidadService disponibilidadService;

    @QueryMapping
    public List<Vehiculo> vehiculosDisponibles(
            @Argument TipoVehiculo tipo,
            @Argument String marca,
            @Argument String modelo,
            @Argument Double precioMin,
            @Argument Double precioMax,
            @Argument String fechaInicio,
            @Argument String fechaFinal) {

        return disponibilidadService.buscarDisponibles(
            tipo,
            marca,
            modelo,
            precioMin,
            precioMax,
            LocalDateTime.parse(fechaInicio),
            LocalDateTime.parse(fechaFinal)
        );
    }
}