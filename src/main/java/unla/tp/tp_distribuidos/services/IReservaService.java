package unla.tp.tp_distribuidos.services;

import unla.tp.tp_distribuidos.dtos.ReservaDTO;
import unla.tp.tp_distribuidos.dtos.ReservaResponseDTO;
import unla.tp.tp_distribuidos.models.Reserva;

public interface IReservaService {
    
    ReservaResponseDTO createReserva(ReservaDTO dto) throws Exception;
    void cancelarReserva(Long idReserva) throws Exception;
}