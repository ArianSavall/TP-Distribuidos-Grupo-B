package unla.tp.vehiculos.services;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import unla.tp.vehiculos.config.RabbitMQConfig;
import unla.tp.vehiculos.dtos.EventosVehiculoDTOs;
import unla.tp.vehiculos.entities.Vehiculo;
import unla.tp.vehiculos.enums.EstadoVehiculo;
import unla.tp.vehiculos.enums.ResultadoValidacionExterna;
import unla.tp.vehiculos.enums.ResultadoVerificacion;
import unla.tp.vehiculos.enums.TipoVehiculo;
import grpc.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import unla.tp.vehiculos.mappers.VehiculoMapper;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import unla.tp.vehiculos.repositories.IVehiculoRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@GrpcService
@RequiredArgsConstructor
public class VehiculoService extends VehiculoServiceGrpc.VehiculoServiceImplBase {
    @Autowired
    private IVehiculoRepository vehiculoRepository;

    // Cliente gRPC para hablar con Reservas
    @GrpcClient("servicio-reservas")
    private ReservaServiceGrpc.ReservaServiceBlockingStub reservaStub;

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void createVehiculo(PostVehiculoRequest request, StreamObserver<grpc.Vehiculo> responseObserver ){
        try{
        Vehiculo nuevoVehiculo = new Vehiculo();
        nuevoVehiculo.setMarca(request.getMarca());
        nuevoVehiculo.setModelo(request.getModelo());
        nuevoVehiculo.setPatente(request.getPatente());
        nuevoVehiculo.setTipoVehiculo(TipoVehiculo.valueOf(request.getTipoVehiculo()));
        nuevoVehiculo.setEstaActivo(true);
        nuevoVehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
        nuevoVehiculo.setAnio(request.getAnio());
        nuevoVehiculo.setColor(request.getColor());
        nuevoVehiculo.setPrecioDiario(request.getPrecioDiario());
        nuevoVehiculo.setResultadoVerificacion(ResultadoVerificacion.PENDIENTE);
        nuevoVehiculo.setHabilitado(false);


        if(vehiculoRepository.findByPatente(request.getPatente()).isEmpty()){
            // 2. Guardamos en la base de datos
            Vehiculo guardado = vehiculoRepository.save(nuevoVehiculo);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.RK_VERIFICACION_SOLICITADA,
                    new EventosVehiculoDTOs.VerificacionSolicitadaEvent(guardado.getId(), guardado.getPatente())
            );

            // 3. Mapeamos la respuesta
            grpc.Vehiculo grpcResponse = VehiculoMapper.toProto(guardado);

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        }else{
            responseObserver.onError(
                    Status.ALREADY_EXISTS
                            .withDescription("Ya hay un vehículo con esa patente.")
                            .asRuntimeException()
            );
        }
        }catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error al persistir el vehículo: " + e.getMessage())
                            .asRuntimeException()
            );
        }

    }
    @Override
    public void saveVehiculo(PatchVehiculoRequest request, StreamObserver<grpc.Vehiculo> responseObserver) {
        try {
            // 1. Mapeamos el request gRPC a nuestra entidad JPA
            Vehiculo nuevoVehiculo = new Vehiculo();
            nuevoVehiculo.setMarca(request.getMarca());
            nuevoVehiculo.setModelo(request.getModelo());
            nuevoVehiculo.setTipoVehiculo(TipoVehiculo.valueOf(request.getTipoVehiculo()));
            nuevoVehiculo.setAnio(request.getAnio());
            nuevoVehiculo.setColor(request.getColor());
            nuevoVehiculo.setPrecioDiario(request.getPrecioDiario());

            if (!nuevoVehiculo.isHabilitado()) {
                throw new IllegalStateException("El vehículo no está habilitado para operar (Estado verificación: "
                        + nuevoVehiculo.getResultadoVerificacion() + ")");
            }

            // 2. Guardamos en la base de datos
            Vehiculo guardado = vehiculoRepository.save(nuevoVehiculo);

            // 3. Mapeamos la respuesta
            grpc.Vehiculo grpcResponse = VehiculoMapper.toProto(guardado);

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();

        }
        catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Error al persistir el vehículo: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }
    @Override
    public void getVehiculosDisponibles(GetVehiculosDisponiblesRequest request, StreamObserver<VehiculoListResponse> responseObserver) {
        try {
            // 1. Extraer fechas del request (asumiendo que las pasaste a String o LocalDateTime)2026-10-20T10:00:00
            LocalDateTime fechaDesde = LocalDateTime.of(request.getFechaDesde().getYear(),request.getFechaDesde().getMonth(),
                    request.getFechaDesde().getDay(), request.getFechaDesde().getHours(), request.getFechaDesde().getMinutes());

            LocalDateTime fechaHasta = LocalDateTime.of(request.getFechaHasta().getYear(), request.getFechaHasta().getMonth(),
                    request.getFechaHasta().getDay(), request.getFechaHasta().getHours(),
                    request.getFechaHasta().getMinutes());

            // 2. Preguntar al servicio de reservas qué vehículos están OCUPADOS en esas fechas
            FechasRequest fechasRequest = FechasRequest.newBuilder()
                    .setFechaDesde(fechaDesde.toString()).setFechaHasta(fechaHasta.toString())
                    .build();

            VehiculosOcupadosResponse ocupadosResponse = reservaStub.getVehiculosOcupados(fechasRequest);
            List<String> patentesOcupadas = ocupadosResponse.getPatentesList();

            // 3. Buscar en la base de datos PROPIA de vehículos
            List<Vehiculo> vehiculosDisponibles;
            if (patentesOcupadas.isEmpty()) {
                vehiculosDisponibles = vehiculoRepository.findAll();
            } else {
                vehiculosDisponibles = vehiculoRepository.findDisponiblesExcluyendo(patentesOcupadas);
            }
            // 4. Aplicamos filtros por precioDiarioMin y precioDiarioMax, marca y modelo
            System.out.println(request.getTipoVehiculo());
            List<Vehiculo> filtradoVehiculos = new ArrayList<>();
            for(Vehiculo v : vehiculosDisponibles){
                boolean coincideTipo = (request.getTipoVehiculo().equals(v.getTipoVehiculo().toString()) || request.getTipoVehiculo().isEmpty());
                boolean coincideMarca = (request.getMarca().isEmpty()) || v.getMarca().equals(request.getMarca());
                boolean coincideModelo = (request.getModelo().isEmpty()) || v.getModelo().equals(request.getModelo());
                boolean estaEntrePrecios =(v.getPrecioDiario()<=request.getPrecioDiarioMax() || request.getPrecioDiarioMax() == 0)
                                            &&
                                            (v.getPrecioDiario()>=request.getPrecioDiarioMin() || request.getPrecioDiarioMin() == 0);
                if(coincideTipo && coincideMarca && coincideModelo && estaEntrePrecios && v.isEstaActivo() && v.isHabilitado()) {
                    filtradoVehiculos.add(v);
                }
            }

            // 5. Mapear a Protobuf y devolver al API Gateway
            VehiculoListResponse.Builder responseBuilder = VehiculoListResponse.newBuilder();
            for (Vehiculo v : filtradoVehiculos) {
                responseBuilder.addVehiculos(
                        VehiculoMapper.toProto(v)
                );
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Error: " + e.getMessage()).asRuntimeException());
        }
    }
    @Override
    public void getAllVehiculos(empty request, StreamObserver<VehiculoListResponse> responseObserver) {
        // 1. Encontrar todos los vehiculos
        List<Vehiculo> vehiculos = vehiculoRepository.findAll();



        // 2. Crear instancias de Vehiculo (Protobuf)
        List<grpc.Vehiculo> vehiculosGrpc = new ArrayList<>();

        for(Vehiculo v : vehiculos){
            if(v.isEstaActivo())
                vehiculosGrpc.add(VehiculoMapper.toProto(v));
        }
        // 3. Ensamblar la respuesta de lista
        VehiculoListResponse response = VehiculoListResponse.newBuilder().addAllVehiculos(vehiculosGrpc) // addVehiculos se genera automáticamente por el "repeated"
                .build();

        // 4. Retornar y completar
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getVehiculoById(GetVehiculoIdRequest request, StreamObserver<grpc.Vehiculo> responseObserver) {
        Optional<Vehiculo> vehiculoOpt = vehiculoRepository.findById(request.getId());

        if (vehiculoOpt.isPresent()) {
            Vehiculo dbVehiculo = vehiculoOpt.get();

                // Retornamos por gRPC
            responseObserver.onNext(VehiculoMapper.toProto(dbVehiculo));
                responseObserver.onCompleted();
        } else {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("El vehículo no existe en la base de datos.")
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void getVehiculoByPatente(GetVehiculoPatenteRequest request, StreamObserver<grpc.Vehiculo> responseObserver) {
        Optional<Vehiculo> vehiculoOpt = vehiculoRepository.findByPatente(request.getPatente());

        if (vehiculoOpt.isPresent()) {
            Vehiculo dbVehiculo = vehiculoOpt.get();

            // Las consultas de datos también deben poder recuperar vehículos
            // inactivos para mostrar reservas históricas.
            responseObserver.onNext(VehiculoMapper.toProto(dbVehiculo));
            responseObserver.onCompleted();

        } else {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("El vehículo no existe en la base de datos.")
                            .asRuntimeException()
            );
        }
    }
    @Override
    public void deleteVehiculo(GetVehiculoPatenteRequest request, StreamObserver<grpc.Vehiculo> responseObserver){

        if (vehiculoRepository.findByPatente(request.getPatente()).isPresent()) {
            Vehiculo vehiculo = vehiculoRepository.findByPatente(request.getPatente()).get();

            vehiculo.setEstaActivo(false);

            Vehiculo vehiculoBaja = vehiculoRepository.save(vehiculo);

            responseObserver.onNext(VehiculoMapper.toProto(vehiculoBaja));
            responseObserver.onCompleted();
        }else {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("El vehículo no existe en la base de datos.")
                            .asRuntimeException()
            );
        }

    }

    @Override
    public void registrarVehiculoExterno(RegistrarVehiculoExternoRequest request,StreamObserver<grpc.VehiculoExterno> observer){
        try{
            String patente = request.getPatente().trim().toUpperCase();

            if(vehiculoRepository.findByPatente(patente).isPresent()){
                observer.onError(Status.ALREADY_EXISTS.withDescription("Ya existe un vehículo con esa patente.").asRuntimeException());
                return;
            }

            Vehiculo vehiculo = new Vehiculo();
            vehiculo.setPatente(patente);
            vehiculo.setMarca(request.getMarca());
            vehiculo.setModelo(request.getModelo());
            vehiculo.setAnio(request.getAnio());
            vehiculo.setColor(request.getColor());
            vehiculo.setTipoVehiculo(TipoVehiculo.valueOf(request.getTipoVehiculo()));
            vehiculo.setPrecioDiario(request.getPrecioDiario());
            vehiculo.setClienteId(request.getClienteId());

            vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
            vehiculo.setEstaActivo(false);
            vehiculo.setHabilitado(false);

            vehiculo.setResultadoVerificacion(ResultadoVerificacion.VALIDADO);
            vehiculo.setResultadoValidacionPatente(ResultadoValidacionExterna.PENDIENTE);
            vehiculo.setResultadoValidacionTitular(ResultadoValidacionExterna.PENDIENTE);

            Vehiculo guardado = vehiculoRepository.save(vehiculo);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.RK_EXTERNOS_REGISTRADOS,
                    new EventosVehiculoDTOs.VehiculoExternoRegistradoEvent(
                            guardado.getId(),
                            guardado.getClienteId(),
                            guardado.getPatente()
                    )
            );

            observer.onNext(grpc.VehiculoExterno.newBuilder()
                    .setId(guardado.getId())
                    .setMarca(guardado.getMarca())
                    .setModelo(guardado.getModelo())
                    .setPatente(guardado.getPatente())
                    .setTipoVehiculo(guardado.getTipoVehiculo().name())
                    .setPrecioDiario(guardado.getPrecioDiario())
                    .setAnio(guardado.getAnio())
                    .setColor(guardado.getColor() == null ? "" : guardado.getColor())
                    .setEstado(guardado.getEstado().name())
                    .setEstaActivo(guardado.isEstaActivo())
                    .setResultadoValidacionPatente(guardado.getResultadoValidacionPatente().name())
                    .setValidacionTitularidad(guardado.getResultadoValidacionTitular().name())
                    .setHabilitado(guardado.isHabilitado())
                    .setClienteId(guardado.getClienteId())
                    .build());
            observer.onCompleted();
            
        }catch (IllegalArgumentException e) {
            observer.onError(Status.INVALID_ARGUMENT
                    .withDescription("Tipo de vehiculo invalido.")
                    .asRuntimeException());
        } catch (Exception e) {
            observer.onError(Status.INTERNAL
                    .withDescription("Error al iniciar la incorporación: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}
