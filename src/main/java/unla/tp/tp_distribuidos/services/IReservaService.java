package unla.tp.tp_distribuidos.services;

import unla.tp.tp_distribuidos.dtos.ReservaDTO;
import unla.tp.tp_distribuidos.dtos.ReservaResponseDTO;
import unla.tp.tp_distribuidos.dtos.ReservaFiltroGraphQLDTO;
import unla.tp.tp_distribuidos.dtos.ReservaGraphQLDTO;
import unla.tp.tp_distribuidos.models.Reserva;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface IReservaService {
    
    ReservaResponseDTO createReserva(ReservaDTO dto) throws Exception;
    void cancelarReserva(Long idReserva) throws Exception;
    List<ReservaGraphQLDTO> buscarReservas(ReservaFiltroGraphQLDTO filtros,
                                          Authentication authentication);
}