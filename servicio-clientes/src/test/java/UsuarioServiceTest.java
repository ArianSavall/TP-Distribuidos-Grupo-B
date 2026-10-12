import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import entities.Usuario;
import entities.UsuarioMetaDatos;
import enums.Rol;
import grpc.ClienteDTO;
import grpc.ClienteIdRequest;
import grpc.ClienteRequest;
import grpc.ClienteResponse;
import grpc.MetadatosRequest;
import grpc.ModificarClienteIdRequest;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import repositories.IUsuarioRepository;
import services.implementation.UsuarioService;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private IUsuarioRepository usuarioRepository;

    @Mock
    private StreamObserver<ClienteResponse> responseObserver;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        UsuarioMetaDatos metadatos = new UsuarioMetaDatos();
        metadatos.setId(10L);
        metadatos.setUsuario("admin_test");
        metadatos.setPassword("password_hash");
        metadatos.setRol(Rol.CLIENTE);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setDni("12345678");
        usuario.setNombre("Carlos");
        usuario.setApellido("Santana");
        usuario.setEmail("carlos.santana@test.com");
        usuario.setTelefono("1122334455");
        usuario.setFechaNacimiento(LocalDate.of(1980, 3, 5));
        usuario.setEstaActivo(true);
        usuario.setMetadatos(metadatos);
    }

    @Test
    void crearCliente_deberiaDevolverElClienteCreado() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario guardado = invocation.getArgument(0);
            guardado.setId(1L);
            guardado.getMetadatos().setId(10L);
            return guardado;
        });

        ClienteRequest request = ClienteRequest.newBuilder()
                .setDni("12345678")
                .setNombre("Carlos")
                .setApellido("Santana")
                .setEmail("carlos.santana@test.com")
                .setTelefono("1122334455")
                .setFechaNacimiento("1980-03-05")
                .setMetadatos(MetadatosRequest.newBuilder()
                        .setUsuario("admin_test")
                        .setPassword("12345password")
                        .build())
                .build();

        usuarioService.crearCliente(request, responseObserver);

        ArgumentCaptor<ClienteResponse> captor = ArgumentCaptor.forClass(ClienteResponse.class);
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();
        verify(responseObserver, never()).onError(any());

        ClienteDTO cliente = captor.getValue().getCliente();
        assertEquals(1L, cliente.getId());
        assertEquals("Carlos", cliente.getNombre());
        assertEquals("carlos.santana@test.com", cliente.getEmail());
        assertEquals("admin_test", cliente.getMetadatos().getUsuario());
        assertEquals("CLIENTE", cliente.getMetadatos().getRol());
    }

    @Test
    void consultarClienteId_deberiaDevolverElClienteCuandoExiste() {
        when(usuarioRepository.findById(1L)).thenReturn(usuario);

        usuarioService.consultarClienteId(
                ClienteIdRequest.newBuilder().setId(1L).build(),
                responseObserver);

        ArgumentCaptor<ClienteResponse> captor = ArgumentCaptor.forClass(ClienteResponse.class);
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();

        ClienteDTO cliente = captor.getValue().getCliente();
        assertEquals(1L, cliente.getId());
        assertEquals("Carlos", cliente.getNombre());
        assertEquals("carlos.santana@test.com", cliente.getEmail());
        assertEquals("admin_test", cliente.getMetadatos().getUsuario());
    }

    @Test
    void consultarClienteId_deberiaInformarNotFoundCuandoNoExiste() {
        when(usuarioRepository.findById(99L)).thenReturn(null);

        usuarioService.consultarClienteId(
                ClienteIdRequest.newBuilder().setId(99L).build(),
                responseObserver);

        ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(captor.capture());
        verify(responseObserver, never()).onNext(any());

        StatusRuntimeException error = assertInstanceOf(
                StatusRuntimeException.class, captor.getValue());
        assertEquals(Status.Code.NOT_FOUND, error.getStatus().getCode());
    }

    @Test
    void modificarClienteId_deberiaGuardarYDevolverLosCambios() {
        when(usuarioRepository.findById(1L)).thenReturn(usuario);
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ModificarClienteIdRequest request = ModificarClienteIdRequest.newBuilder()
                .setId(1L)
                .setDni("12345678")
                .setNombre("Carlos Modificado")
                .setApellido("Santana")
                .setEmail("carlos.santana@test.com")
                .setTelefono("1122334455")
                .setFechaNacimiento("1980-03-05")
                .setMetadatos(MetadatosRequest.newBuilder()
                        .setUsuario("admin_test")
                        .setPassword("nueva_password")
                        .build())
                .build();

        usuarioService.modificarClienteId(request, responseObserver);

        ArgumentCaptor<ClienteResponse> captor = ArgumentCaptor.forClass(ClienteResponse.class);
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();
        verify(usuarioRepository).save(usuario);

        ClienteDTO cliente = captor.getValue().getCliente();
        assertEquals("Carlos Modificado", cliente.getNombre());
        assertEquals("admin_test", cliente.getMetadatos().getUsuario());
    }

    @Test
    void borrarClienteId_deberiaDarDeBajaAlCliente() {
        when(usuarioRepository.findById(1L)).thenReturn(usuario);
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        usuarioService.borrarClienteId(
                ClienteIdRequest.newBuilder().setId(1L).build(),
                responseObserver);

        ArgumentCaptor<ClienteResponse> captor = ArgumentCaptor.forClass(ClienteResponse.class);
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();
        verify(usuarioRepository, times(1)).save(usuario);

        assertFalse(captor.getValue().getCliente().getEstaActivo());
        assertFalse(usuario.getEstaActivo());
    }
}