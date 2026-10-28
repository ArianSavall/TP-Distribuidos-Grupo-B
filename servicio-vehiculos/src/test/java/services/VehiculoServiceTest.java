package services;

import unla.tp.vehiculos.entities.Vehiculo;
import unla.tp.vehiculos.enums.EstadoVehiculo;
import unla.tp.vehiculos.enums.TipoVehiculo;
import grpc.*;
import grpc.ReservaServiceGrpc;
import grpc.GetVehiculosOcupadosResponse;
import unla.tp.vehiculos.repositories.IVehiculoRepository;
import io.grpc.stub.StreamObserver;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unla.tp.vehiculos.services.VehiculoService;

import java.util.List;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehiculoServiceTest {

    // Mockeamos el acceso a BD basado en la interfaz del repositorio
    @Mock
    private IVehiculoRepository vehiculoRepository;

    // Mockeamos el canal de respuesta gRPC
    @Mock
    private StreamObserver<grpc.Vehiculo> responseObserver;

    // Inyectamos los mocks en el servicio a testear
    @InjectMocks
    private VehiculoService vehiculoService;

    private Vehiculo vehiculoMock;

    @Mock
    private ReservaServiceGrpc.ReservaServiceBlockingStub reservaStub;

    @Mock
    private StreamObserver<VehiculoListResponse> listResponseObserver;

    @BeforeEach
    void setUp() {
        // Preparamos la entidad JPA que devolverá el repositorio falso[cite: 9]
        vehiculoMock = new Vehiculo();
        vehiculoMock.setId(1L);
        vehiculoMock.setMarca("Ford");
        vehiculoMock.setModelo("Focus");
        vehiculoMock.setTipoVehiculo(TipoVehiculo.SUV);
        vehiculoMock.setColor("Rojo");
        vehiculoMock.setAnio(2020);
        vehiculoMock.setPatente("AB123CD");
        vehiculoMock.setEstado(EstadoVehiculo.DISPONIBLE);
        vehiculoMock.setPrecioDiario(1500.0);
        vehiculoMock.setEstaActivo(true);
    }

    @Test
    void getVehiculoById_Existente_RetornaVehiculo() {
        // Arrange: Preparamos la petición Protobuf
        GetVehiculoIdRequest request = GetVehiculoIdRequest.newBuilder().setId(1L).build();
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(vehiculoMock));

        // Act: Ejecutamos el método del servicio
        vehiculoService.getVehiculoById(request, responseObserver);

        // Assert: Capturamos y verificamos la respuesta de red
        ArgumentCaptor<grpc.Vehiculo> captor = ArgumentCaptor.forClass(grpc.Vehiculo.class);

        verify(responseObserver).onNext(captor.capture()); // Verificamos que envió un dato
        verify(responseObserver).onCompleted(); // Verificamos que cerró la conexión
        verify(responseObserver, never()).onError(any());

        grpc.Vehiculo response = captor.getValue();
        assertEquals("Ford", response.getMarca());
        assertEquals("AB123CD", response.getPatente());
    }

    @Test
    void getVehiculoById_NoExistente_LanzaError() {
        // Arrange
        GetVehiculoIdRequest request = GetVehiculoIdRequest.newBuilder().setId(99L).build();
        when(vehiculoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        vehiculoService.getVehiculoById(request, responseObserver);

        // Assert
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);

        verify(responseObserver).onError(errorCaptor.capture()); // Verificamos que envió el error
        verify(responseObserver, never()).onNext(any()); // Verificamos que nunca envió datos

        Throwable error = errorCaptor.getValue();
        assertInstanceOf(StatusRuntimeException.class, error);
        assertTrue(error.getMessage().contains("NOT_FOUND"));
    }

    @Test
    void saveVehiculo_DatosValidos_GuardaYRetornaVehiculo() {
        // Arrange: Petición desde el API Gateway
        PatchVehiculoRequest request = PatchVehiculoRequest.newBuilder()
                .setMarca("Chevrolet")
                .setModelo("Cruze")
                .setAnio(2023)
                .setColor("Blanco")
                .setTipoVehiculo(TipoVehiculo.SUV.toString())
                .setPrecioDiario(15000.0)
                .build();

        // Simulamos el comportamiento de Hibernate al guardar (nos devuelve la entidad con ID)
        Vehiculo vehiculoGuardado = new Vehiculo();
        vehiculoGuardado.setId(10L);
        vehiculoGuardado.setMarca("Chevrolet");
        vehiculoGuardado.setModelo("Cruze");
        vehiculoGuardado.setAnio(2023);
        vehiculoGuardado.setTipoVehiculo(TipoVehiculo.SUV);
        vehiculoGuardado.setColor("Blanco");
        vehiculoGuardado.setPrecioDiario(15000.0);
        vehiculoGuardado.setPatente("SIN-PATENTE");
        vehiculoGuardado.setEstado(EstadoVehiculo.DISPONIBLE);
        vehiculoGuardado.setEstaActivo(true);

        when(vehiculoRepository.save(any(Vehiculo.class))).thenReturn(vehiculoGuardado);

        // Act
        vehiculoService.saveVehiculo(request, responseObserver);

        // Assert: Verificamos el mapeo final al mensaje gRPC
        ArgumentCaptor<grpc.Vehiculo> captor = ArgumentCaptor.forClass(grpc.Vehiculo.class);
        verify(vehiculoRepository).save(any(Vehiculo.class)); // Asegura que tocó la base de datos
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();

        grpc.Vehiculo response = captor.getValue();
        assertEquals(10L, response.getId());
        assertEquals("Chevrolet", response.getMarca());
        assertEquals(15000.0, response.getPrecioDiario()); // Recordando que está como String en tu .proto
    }

    // --- TESTS PARA DISPONIBILIDAD DISTRIBUIDA ---

    @Test
    void getVehiculosDisponibles_SinOcupados_RetornaTodosLosActivos() {
        // Arrange: El cliente pide disponibilidad
        GetVehiculosDisponiblesRequest request = GetVehiculosDisponiblesRequest.newBuilder().build();

        // 1. Simulamos que el servicio de reservas responde que no hay vehículos ocupados
        GetVehiculosOcupadosResponse ocupadosMock = GetVehiculosOcupadosResponse.newBuilder().build(); // Lista vacía
        when(reservaStub.getVehiculosOcupados(any())).thenReturn(ocupadosMock);

        // 2. Simulamos que la base de datos propia devuelve 2 vehículos activos
        Vehiculo v2 = new Vehiculo();
        v2.setId(2L);
        v2.setMarca("Fiat");

        when(vehiculoRepository.findAll()).thenReturn(List.of(vehiculoMock, v2));

        // Act
        vehiculoService.getVehiculosDisponibles(request, listResponseObserver);

        // Assert
        ArgumentCaptor<VehiculoListResponse> captor = ArgumentCaptor.forClass(VehiculoListResponse.class);
        verify(vehiculoRepository).findAll();
        verify(vehiculoRepository, never()).findDisponiblesExcluyendo(anyList()); // No debió llamar al filtro excluyente
        verify(listResponseObserver).onNext(captor.capture());
        verify(listResponseObserver).onCompleted();

        VehiculoListResponse response = captor.getValue();
        assertEquals(2, response.getVehiculosCount());
        assertEquals("Ford", response.getVehiculos(0).getMarca()); // vehiculoMock del @BeforeEach
        assertEquals("Fiat", response.getVehiculos(1).getMarca());
    }

    @Test
    void getVehiculosDisponibles_ConOcupados_AplicaFiltroYRetorna() {
        // Arrange
        GetVehiculosDisponiblesRequest request = GetVehiculosDisponiblesRequest.newBuilder().build();

        // 1. Simulamos que el servicio de reservas indica que el ID 2 está ocupado
        GetVehiculosOcupadosResponse ocupadosMock = GetVehiculosOcupadosResponse.newBuilder()
                .addPatentes("AB123CD")
                .build();
        when(reservaStub.getVehiculosOcupados(any())).thenReturn(ocupadosMock);

        // 2. Simulamos que nuestra base de datos, al excluir el ID 2, solo devuelve nuestro vehiculoMock (ID 1)[cite: 9]
        when(vehiculoRepository.findDisponiblesExcluyendo(List.of("AB123CD"))).thenReturn(List.of(vehiculoMock));

        // Act
        vehiculoService.getVehiculosDisponibles(request, listResponseObserver);

        // Assert
        ArgumentCaptor<VehiculoListResponse> captor = ArgumentCaptor.forClass(VehiculoListResponse.class);
        verify(vehiculoRepository).findDisponiblesExcluyendo(List.of("AB123CD"));
        verify(vehiculoRepository, never()).findAll(); // No debió llamar al listado completo
        verify(listResponseObserver).onNext(captor.capture());
        verify(listResponseObserver).onCompleted();

        VehiculoListResponse response = captor.getValue();
        assertEquals(1, response.getVehiculosCount());
        assertEquals(1L, response.getVehiculos(0).getId());
    }
}