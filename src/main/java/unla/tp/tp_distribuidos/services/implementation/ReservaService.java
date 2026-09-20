package unla.tp.tp_distribuidos.services.implementation;

import org.modelmapper.ModelMapper; 
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import unla.tp.tp_distribuidos.dtos.ReservaDTO;
import unla.tp.tp_distribuidos.dtos.ReservaResponseDTO; 
import unla.tp.tp_distribuidos.models.*;
import unla.tp.tp_distribuidos.enums.EstadoReserva;
import unla.tp.tp_distribuidos.repositories.*;
import unla.tp.tp_distribuidos.services.IReservaService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class ReservaService implements IReservaService {

    private final IReservaRepository reservaRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IVehiculoRepository vehiculoRepository;
    
    private ModelMapper modelMapper = new ModelMapper(); 

    public ReservaService(IReservaRepository reservaRepository, 
                          IUsuarioRepository usuarioRepository, 
                          IVehiculoRepository vehiculoRepository) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.vehiculoRepository = vehiculoRepository;
    }

    @Override
    @Transactional
    public ReservaResponseDTO createReserva(ReservaDTO dto) throws Exception { 
        if (dto.getFechaHoraInicio() == null || dto.getFechaHoraFinal() == null) {
            throw new Exception("ERROR: Las fechas son obligatorias.");
        }
        if (dto.getFechaHoraInicio().isBefore(LocalDateTime.now())) {
            throw new Exception("ERROR: La fecha de inicio debe ser futura.");
        }
        if (!dto.getFechaHoraFinal().isAfter(dto.getFechaHoraInicio())) {
            throw new Exception("ERROR: La fecha de finalización debe ser posterior a la de inicio.");
        }

        Usuario cliente = usuarioRepository.findById(dto.getIdCliente().getId());

        if (cliente == null) {
            throw new Exception("ERROR: Cliente no encontrado.");
        }
        if (cliente.getEstaActivo() == null || !cliente.getEstaActivo()) {
            throw new Exception("ERROR: El cliente no se encuentra activo.");
        }

        Vehiculo vehiculo = vehiculoRepository.findById(dto.getIdVehiculo().getId()).orElseThrow(() -> new Exception("ERROR: Vehículo no encontrado."));
        if (!vehiculo.isEstaActivo()) {
            throw new Exception("ERROR: El vehículo no se encuentra activo.");
        }

        long solapadas = reservaRepository.countReservasSolapadas(
                vehiculo.getId(), 
                EstadoReserva.CONFIRMADO, 
                dto.getFechaHoraInicio(), 
                dto.getFechaHoraFinal()
        );
        
        if (solapadas > 0) {
            throw new Exception("ERROR: El vehículo no está disponible en el período solicitado.");
        }

        long diasAlquiler = ChronoUnit.DAYS.between(dto.getFechaHoraInicio(), dto.getFechaHoraFinal());
        if (diasAlquiler == 0) diasAlquiler = 1;
        double importeTotal = diasAlquiler * vehiculo.getPrecioDiario();

        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setVehiculo(vehiculo);
        reserva.setFechaHoraInicio(dto.getFechaHoraInicio());
        reserva.setFechaHoraFinal(dto.getFechaHoraFinal());
        reserva.setImporteTotal(importeTotal);
        reserva.setEstadoReserva(EstadoReserva.CONFIRMADO);

        Reserva reservaGuardada = reservaRepository.save(reserva);
        
        return modelMapper.map(reservaGuardada, ReservaResponseDTO.class);
    }

    @Override
    @Transactional
    public void cancelarReserva(Long idReserva) throws Exception {
        Reserva reserva = reservaRepository.findById(idReserva).orElseThrow(() -> new Exception("ERROR: Reserva no encontrada."));

        LocalDateTime ahora = LocalDateTime.now();
        if (ahora.isAfter(reserva.getFechaHoraInicio()) || ahora.isEqual(reserva.getFechaHoraInicio())) {
            throw new Exception("ERROR: No se puede cancelar la reserva porque el período de alquiler ya ha comenzado.");
        }

        if (reserva.getEstadoReserva() == EstadoReserva.CANCELADO) {
            throw new Exception("ERROR: La reserva ya se encuentra cancelada.");
        }

        reserva.setEstadoReserva(EstadoReserva.CANCELADO);
        
        reservaRepository.save(reserva);
    }
}