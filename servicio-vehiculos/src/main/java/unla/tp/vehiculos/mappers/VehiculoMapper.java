package unla.tp.vehiculos.mappers;

import unla.tp.vehiculos.entities.Vehiculo;
import unla.tp.vehiculos.enums.TipoVehiculo;
import java.util.List;

public final class VehiculoMapper {

    private VehiculoMapper() {
    }

    public static grpc.Vehiculo toProto(Vehiculo v) {
        return grpc.Vehiculo.newBuilder()
                .setId(value(v.getId()))
                .setPatente(value(v.getPatente()))
                .setMarca(value(v.getMarca()))
                .setModelo(value(v.getModelo()))
                .setAnio(v.getAnio())
                .setTipoVehiculo(tipo(v))
                .setPrecioDiario(v.getPrecioDiario())
                .setEstado(estado(v))
                .setEstaActivo(v.isEstaActivo())
                .setResultadoValidacionPatente(validacionPatente(v))
                .setResultadoValidacionTitular(validacionTitular(v))
                .setHabilitado(v.isHabilitado())
                .setResultadoVerificacion(resultadoVerificacion(v))
                .setColor(value(v.getColor()))
                .build();
    }

    public static grpc.GetVehiculoByIdResponse toProtoGetVehiculoByIdResponse(Vehiculo v) {
        return grpc.GetVehiculoByIdResponse.newBuilder()
                .setId(value(v.getId())).setPatente(value(v.getPatente()))
                .setMarca(value(v.getMarca())).setModelo(value(v.getModelo()))
                .setAnio(v.getAnio()).setTipoVehiculo(tipo(v)).setPrecioDiario(v.getPrecioDiario())
                .setEstado(estado(v)).setEstaActivo(v.isEstaActivo()).setColor(value(v.getColor()))
                .setResultadoValidacionPatente(validacionPatente(v))
                .setResultadoValidacionTitular(validacionTitular(v)).setHabilitado(v.isHabilitado())
                .setResultadoVerificacion(resultadoVerificacion(v))
                .build();
    }

    public static grpc.GetVehiculoByPatenteResponse toProtoGetVehiculoByPatenteResponse(Vehiculo v) {
        return grpc.GetVehiculoByPatenteResponse.newBuilder()
                .setId(value(v.getId())).setPatente(value(v.getPatente()))
                .setMarca(value(v.getMarca())).setModelo(value(v.getModelo()))
                .setAnio(v.getAnio()).setTipoVehiculo(tipo(v)).setPrecioDiario(v.getPrecioDiario())
                .setEstado(estado(v)).setEstaActivo(v.isEstaActivo()).setColor(value(v.getColor()))
                .setResultadoValidacionPatente(validacionPatente(v))
                .setResultadoValidacionTitular(validacionTitular(v)).setHabilitado(v.isHabilitado())
                .setResultadoVerificacion(resultadoVerificacion(v))
                .build();
    }

    public static grpc.CreateVehiculoResponse toProtoCreateVehiculoResponse(Vehiculo v) {
        return grpc.CreateVehiculoResponse.newBuilder()
                .setId(value(v.getId())).setPatente(value(v.getPatente()))
                .setMarca(value(v.getMarca())).setModelo(value(v.getModelo()))
                .setAnio(v.getAnio()).setTipoVehiculo(tipo(v)).setPrecioDiario(v.getPrecioDiario())
                .setEstado(estado(v)).setEstaActivo(v.isEstaActivo()).setColor(value(v.getColor()))
                .setResultadoVerificacion(resultadoVerificacion(v))
                .build();
    }

    public static grpc.SaveVehiculoResponse toProtoSaveVehiculoResponse(Vehiculo v) {
        return grpc.SaveVehiculoResponse.newBuilder()
                .setMarca(value(v.getMarca())).setModelo(value(v.getModelo()))
                .setAnio(v.getAnio()).setTipoVehiculo(tipo(v))
                .setPrecioDiario(v.getPrecioDiario()).setColor(value(v.getColor()))
                .setResultadoVerificacion(resultadoVerificacion(v))
                .build();
    }

    public static grpc.DeleteVehiculoResponse toProtoDeleteVehiculoResponse(Vehiculo v) {
        return grpc.DeleteVehiculoResponse.newBuilder()
                .setId(value(v.getId())).setPatente(value(v.getPatente()))
                .setMarca(value(v.getMarca())).setModelo(value(v.getModelo()))
                .setAnio(v.getAnio()).setTipoVehiculo(tipo(v)).setPrecioDiario(v.getPrecioDiario())
                .setEstado(estado(v)).setEstaActivo(v.isEstaActivo()).setColor(value(v.getColor()))
                .setResultadoVerificacion(resultadoVerificacion(v))
                .build();
    }

    public static grpc.GetAllVehiculosResponse toProtoGetAllVehiculosResponse(List<Vehiculo> vehiculos) {
        return grpc.GetAllVehiculosResponse.newBuilder()
                .addAllVehiculos(vehiculos.stream().map(VehiculoMapper::toProto).toList())
                .build();
    }

    public static grpc.GetVehiculosDisponiblesResponse toProtoGetVehiculosDisponiblesResponse(
            List<Vehiculo> vehiculos) {
        return grpc.GetVehiculosDisponiblesResponse.newBuilder()
                .addAllVehiculos(vehiculos.stream().map(VehiculoMapper::toProto).toList())
                .build();
    }

    public static grpc.RegistrarVehiculoExternoResponse toProtoRegistrarVehiculoExternoResponse(Vehiculo v,
                                                                                                  String estadoIncorporacion) {
        return grpc.RegistrarVehiculoExternoResponse.newBuilder()
                .setId(value(v.getId())).setPatente(value(v.getPatente()))
                .setMarca(value(v.getMarca())).setModelo(value(v.getModelo()))
                .setAnio(v.getAnio()).setTipoVehiculo(tipo(v)).setPrecioDiario(v.getPrecioDiario())
                .setEstado(estado(v)).setEstaActivo(v.isEstaActivo()).setColor(value(v.getColor()))
                .setEstadoIncorporacion(value(estadoIncorporacion))
                .setResultadoValidacionPatente(enumValue(grpc.ResultadoVerificacion.RESULTADO_VERIFICACION_UNSPECIFIED,
                        "RESULTADO_VERIFICACION_", v.getResultadoVerificacion() == null
                                ? null : v.getResultadoVerificacion().name()))
                .setValidacionTitularidad(validacionTitular(v)).setHabilitado(v.isHabilitado())
                .setClienteId(value(v.getClienteId())).build();
    }

    private static grpc.TipoVehiculo tipo(Vehiculo v) {
        return enumValue(grpc.TipoVehiculo.TIPO_VEHICULO_UNSPECIFIED, "TIPO_VEHICULO_",
                v.getTipoVehiculo() == null ? null : v.getTipoVehiculo().name());
    }

    public static TipoVehiculo toDomain(grpc.TipoVehiculo tipo) {
        return tipo == null || tipo == grpc.TipoVehiculo.TIPO_VEHICULO_UNSPECIFIED
                ? null
                : TipoVehiculo.valueOf(tipo.name().replace("TIPO_VEHICULO_", ""));
    }

    private static grpc.EstadoVehiculo estado(Vehiculo v) {
        return enumValue(grpc.EstadoVehiculo.ESTADO_VEHICULO_UNSPECIFIED, "ESTADO_VEHICULO_",
                v.getEstado() == null ? null : v.getEstado().name());
    }

    private static grpc.ResultadoValidacionExterna validacionPatente(Vehiculo v) {
        return enumValue(grpc.ResultadoValidacionExterna.RESULTADO_VALIDACION_EXTERNA_UNSPECIFIED,
                "RESULTADO_VALIDACION_EXTERNA_",
                v.getResultadoValidacionPatente() == null ? null : v.getResultadoValidacionPatente().name());
    }

    private static grpc.ResultadoVerificacion resultadoVerificacion(Vehiculo v) {
        return enumValue(grpc.ResultadoVerificacion.RESULTADO_VERIFICACION_UNSPECIFIED,
                "RESULTADO_VERIFICACION_", v.getResultadoVerificacion() == null
                        ? null : v.getResultadoVerificacion().name());
    }

    private static grpc.ResultadoValidacionExterna validacionTitular(Vehiculo v) {
        return enumValue(grpc.ResultadoValidacionExterna.RESULTADO_VALIDACION_EXTERNA_UNSPECIFIED,
                "RESULTADO_VALIDACION_EXTERNA_",
                v.getResultadoValidacionTitular() == null ? null : v.getResultadoValidacionTitular().name());
    }

    private static <E extends Enum<E>> E enumValue(E fallback, String prefix, String name) {
        if (name == null) {
            return fallback;
        }
        try {
            return Enum.valueOf(fallback.getDeclaringClass(), prefix + name);
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    private static String value(String value) {
        return value == null ? "" : value;
    }

    private static long value(Long value) {
        return value == null ? 0L : value;
    }

    public static String getEnumStringSanitized(String s){
        return s.replace("TIPO_VEHICULO_","")
                .replace("ESTADO_VEHICULO_","")
                .replace("RESULTADO_VALIDACION_EXTERNA_","")
                .replace("RESULTADO_VERIFICACION_","");
    }
}