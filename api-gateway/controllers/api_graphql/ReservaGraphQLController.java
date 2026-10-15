package unla.tp.tp_distribuidos.controllers.api_graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import unla.tp.tp_distribuidos.dtos.ReservaFiltroGraphQLDTO;
import unla.tp.tp_distribuidos.dtos.ReservaGraphQLDTO;
import unla.tp.tp_distribuidos.services.IReservaService;

import java.util.List;

@Controller
public class ReservaGraphQLController {

    private final IReservaService reservaService;

    public ReservaGraphQLController(IReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @QueryMapping
    public List<ReservaGraphQLDTO> reservas(@Argument ReservaFiltroGraphQLDTO filtros) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return reservaService.buscarReservas(filtros, authentication);
    }
}