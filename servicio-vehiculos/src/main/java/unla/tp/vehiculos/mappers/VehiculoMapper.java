package unla.tp.vehiculos.mappers;

import unla.tp.vehiculos.entities.Vehiculo;
import unla.tp.vehiculos.enums.EstadoVehiculo;
import unla.tp.vehiculos.enums.TipoVehiculo;
public final class VehiculoMapper {

    private VehiculoMapper() {
    }

    public static grpc.Vehiculo toProto(Vehiculo v) {
        grpc.Vehiculo.Builder b = grpc.Vehiculo.newBuilder()
                .setId(v.getId())
                .setPatente(v.getPatente())
                .setMarca(v.getMarca())
                .setModelo(v.getModelo())
                .setAnio(v.getAnio())
                .setTipoVehiculo(v.getTipoVehiculo().toString())
                .setPrecioDiario(v.getPrecioDiario())
                .setEstado(v.getEstado().name())
                .setEstaActivo(v.isEstaActivo());
        if (v.getColor() != null) {
            b.setColor(v.getColor());
        }
        return b.build();
    }

    /** Copia todos los campos del mensaje a la entidad, excepto el id. */
    public static void copiarA(Vehiculo destino, grpc.Vehiculo p) {
        destino.setPatente(p.getPatente());
        destino.setMarca(p.getMarca());
        destino.setModelo(p.getModelo());
        destino.setAnio(p.getAnio());
        destino.setColor(p.getColor());
        destino.setTipoVehiculo(Parsers.enumDe(TipoVehiculo.class, p.getTipoVehiculo(), "tipo_vehiculo"));
        destino.setPrecioDiario(p.getPrecioDiario());
        destino.setEstado(Parsers.enumDe(EstadoVehiculo.class, p.getEstado(), "estado"));
        destino.setEstaActivo(p.getEstaActivo());
    }
}