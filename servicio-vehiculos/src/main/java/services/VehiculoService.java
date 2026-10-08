package services;

import grpc.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;


import java.util.List;
import java.util.Optional;

@GrpcService
public class VehiculoService extends VehiculoServiceGrpc.VehiculoServiceImplBase {
    @Override
    public void saveVehiculo(PutVehiculoRequest request, StreamObserver<Vehiculo> responseObserver) {
        try {
            // 1. Extraer los datos que envía el API Gateway en el PutVehiculoRequest
            String marca = request.getMarca();
            String modelo = request.getModelo();
            int anio = request.getAnio();
            String color = request.getColor();
            String tipoVehiculo = request.getTipoVehiculo();
            double precioDiario = request.getPrecioDiario(); // Nota: En el request es double

            // 2. Delegar la persistencia al servicio de base de datos.
            // Aquí usarías tu cliente gRPC (el stub) para enviar estos datos al servicio-database.
            // Ejemplo conceptual:
            // DbVehiculoResponse dbResponse = databaseStub.insertVehiculo(request);

            // (Para este ejemplo, simulamos los datos autogenerados por la base de datos)
            long idGeneradoPorBD = 101L;
            String patenteGenerada = "AG789XYZ"; // Asumiendo que la patente se genera o valida en la BD

            // 3. Mapear el resultado final al mensaje Vehiculo de Protobuf para responder
            Vehiculo vehiculoResponse = Vehiculo.newBuilder()
                    .setId(idGeneradoPorBD)
                    .setMarca(marca)
                    .setModelo(modelo)
                    .setAnio(anio)
                    .setColor(color)
                    .setTipoVehiculo(tipoVehiculo)
                    // Ojo: En tu mensaje Vehiculo definiste precio_diario como string
                    // por lo que debes convertir el double a String, o corregir el .proto más adelante.
                    .setPrecioDiario(precioDiario)
                    .setPatente(patenteGenerada)
                    .setEstaActivo(true)
                    .setEstado("DISPONIBLE") // Estado inicial por defecto
                    .build();

            // 4. Enviar la respuesta a través del flujo de red
            responseObserver.onNext(vehiculoResponse);

            // 5. Cerrar la comunicación indicando que el proceso fue exitoso
            responseObserver.onCompleted();

        } catch (Exception e) {
            // Manejo de excepciones devolviendo un código de estado gRPC adecuado
            responseObserver.onError(
                    io.grpc.Status.INTERNAL
                            .withDescription("Error interno al intentar guardar el vehículo: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }
    @Override
    public void getAllVehiculos(empty request, StreamObserver<VehiculoListResponse> responseObserver) {

        // 1. Llamar al servicio de base de datos para obtener la lista
        // ...

        // 2. Crear instancias de Vehiculo (Protobuf)
        Vehiculo v1 = Vehiculo.newBuilder().setId(1).setMarca("Fiat").setModelo("Cronos").build();
        Vehiculo v2 = Vehiculo.newBuilder().setId(2).setMarca("Toyota").setModelo("Corolla").build();

        // 3. Ensamblar la respuesta de lista
        VehiculoListResponse response = VehiculoListResponse.newBuilder()
                .addVehiculos(v1) // addVehiculos se genera automáticamente por el "repeated"
                .addVehiculos(v2)
                .build();

        // 4. Retornar y completar
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getVehiculoById(GetVehiculoIdRequest request, StreamObserver<Vehiculo> responseObserver) {
        // 1. Obtener el ID que viene en la petición del API Gateway
        long idBuscado = request.getId();

        try {
            // 2. Aquí llamarías al servicio de base de datos por gRPC
            // Ejemplo conceptual: DbVehiculoResponse dbData = databaseStub.findById(idBuscado);

            // 3. Mapear los datos al objeto de Protobuf (usando el patrón Builder)
            Vehiculo vehiculoResponse = Vehiculo.newBuilder()
                    .setId(idBuscado) // Usamos el ID buscado a modo de ejemplo
                    .setMarca("Ford") // Reemplazar con dbData.getMarca()
                    .setModelo("Focus")
                    .setPatente("AB123CD")
                    .setEstaActivo(true)
                    .build();

            // 4. Enviar la respuesta por la red
            responseObserver.onNext(vehiculoResponse);

            // 5. Avisar que terminamos de transmitir
            responseObserver.onCompleted();

        } catch (Exception e) {
            // Manejo de errores estilo gRPC
            responseObserver.onError(
                    io.grpc.Status.INTERNAL
                            .withDescription("Error al buscar el vehículo: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }


}
