package unla.tp.tp_distribuidos.reservas.services;

import unla.tp.tp_distribuidos.reservas.dtos.ReservaDTO;
import unla.tp.tp_distribuidos.reservas.dtos.ReservaResponseDTO;
import unla.tp.tp_distribuidos.reservas.dtos.ReservaFiltroGraphQLDTO;
import unla.tp.tp_distribuidos.reservas.dtos.ReservaGraphQLDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface IReservaService {
    
    ReservaResponseDTO createReserva(ReservaDTO dto) throws Exception;
    void cancelarReserva(Long idReserva) throws Exception;
    List<ReservaGraphQLDTO> buscarReservas(ReservaFiltroGraphQLDTO filtros,
                                          Authentication authentication);
}