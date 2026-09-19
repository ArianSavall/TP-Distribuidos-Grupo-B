package unla.tp.tp_distribuidos.services.implementation;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import unla.tp.tp_distribuidos.dtos.ReservaDTO;
import unla.tp.tp_distribuidos.dtos.ReservaFiltroGraphQLDTO;
import unla.tp.tp_distribuidos.dtos.ReservaGraphQLDTO;
import unla.tp.tp_distribuidos.dtos.ReservaResponseDTO;
import unla.tp.tp_distribuidos.enums.EstadoReserva;
import unla.tp.tp_distribuidos.models.Reserva;
import unla.tp.tp_distribuidos.models.Usuario;
import unla.tp.tp_distribuidos.models.Vehiculo;
import unla.tp.tp_distribuidos.repositories.IReservaRepository;
import unla.tp.tp_distribuidos.repositories.IUsuarioRepository;
import unla.tp.tp_distribuidos.repositories.IVehiculoRepository;
import unla.tp.tp_distribuidos.services.IReservaService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

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

        Usuario cliente = usuarioRepository.findById(dto.getIdCliente());

        if (cliente == null) {
            throw new Exception("ERROR: Cliente no encontrado.");
        }
        if (cliente.getEstaActivo() == null || !cliente.getEstaActivo()) {
            throw new Exception("ERROR: El cliente no se encuentra activo.");
        }

        Vehiculo vehiculo = vehiculoRepository.findById(dto.getIdVehiculo()).orElseThrow(() -> new Exception("ERROR: Vehículo no encontrado."));
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

    @Override
    @Transactional
    public List<ReservaGraphQLDTO> buscarReservas(ReservaFiltroGraphQLDTO filtros,
                                                   Authentication authentication) {
        ReservaFiltroGraphQLDTO criterios = filtros == null
                ? new ReservaFiltroGraphQLDTO()
                : filtros;
        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        Long clienteAutenticadoId = null;
        if (!esAdmin) {
            Usuario cliente = usuarioRepository.findByMetadatos_Usuario(authentication.getName());
            if (cliente == null) {
                throw new IllegalArgumentException("ERROR: Cliente autenticado no encontrado.");
            }
            clienteAutenticadoId = cliente.getId();
        }

        LocalDateTime fechaDesde = parsearFecha(criterios.getFechaDesde(), "fechaDesde");
        LocalDateTime fechaHasta = parsearFecha(criterios.getFechaHasta(), "fechaHasta");
        if (fechaDesde != null && fechaHasta != null && fechaHasta.isBefore(fechaDesde)) {
            throw new IllegalArgumentException("ERROR: fechaHasta debe ser posterior o igual a fechaDesde.");
        }

        return reservaRepository.buscarReservas(
                        esAdmin ? criterios.getClienteId() : null,
                        clienteAutenticadoId,
                        criterios.getVehiculoId(),
                        criterios.getTipoVehiculo(),
                        criterios.getEstado(),
                        fechaDesde,
                        fechaHasta)
                .stream()
                .map(this::toGraphQLDTO)
                .collect(Collectors.toList());
    }

    private LocalDateTime parsearFecha(String valor, String nombreCampo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(valor);
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "ERROR: " + nombreCampo + " debe tener formato ISO-8601, por ejemplo 2026-09-18T00:00:00.");
        }
    }

    private ReservaGraphQLDTO toGraphQLDTO(Reserva reserva) {
        Usuario cliente = reserva.getCliente();
        Vehiculo vehiculo = reserva.getVehiculo();
        return new ReservaGraphQLDTO(
                cliente.getNombre() + " " + cliente.getApellido(),
                vehiculo.getMarca() + " " + vehiculo.getModelo(),
                vehiculo.getPatente(),
                reserva.getFechaHoraInicio().toString(),
                reserva.getFechaHoraFinal().toString(),
                vehiculo.getPrecioDiario(),
                reserva.getImporteTotal(),
                reserva.getEstadoReserva());
    }
}