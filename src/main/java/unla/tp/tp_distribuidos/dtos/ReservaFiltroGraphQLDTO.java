package unla.tp.tp_distribuidos.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import unla.tp.tp_distribuidos.enums.EstadoReserva;
import unla.tp.tp_distribuidos.enums.TipoVehiculo;

@Getter
@Setter
@NoArgsConstructor
public class ReservaFiltroGraphQLDTO {
    private Long clienteId;
    private Long vehiculoId;
    private TipoVehiculo tipoVehiculo;
    private EstadoReserva estado;
    private String fechaDesde;
    private String fechaHasta;
}