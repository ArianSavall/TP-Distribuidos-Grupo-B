package clientes.services;

import java.time.LocalDate;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;
import clientes.entities.Usuario;
import clientes.entities.UsuarioMetaDatos;
import grpc.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import clientes.repositories.IUsuarioRepository;


@GrpcService
public class UsuarioService extends ClienteServiceGrpc.ClienteServiceImplBase{


    private IUsuarioRepository usuarioRepository;


    public UsuarioService(IUsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void listarUsuarios(ListarUsuariosRequest request, StreamObserver<ListarUsuariosResponse> responseObserver) {
        try {
            //Logica para obtener la lista de usuarios desde la base de datos
            List<Usuario> usuarios = usuarioRepository.findAllWithMetadatos();
            
             
            ListarUsuariosResponse.Builder responseBuilder = ListarUsuariosResponse.newBuilder();

            
            for (Usuario u : usuarios) {
                Metadatos metaDto = Metadatos.newBuilder()
                        .setId(u.getMetadatos().getId())
                        .setUsuario(u.getMetadatos().getUsuario())
                        .setPassword(u.getMetadatos().getPassword())
                        .setRol(u.getMetadatos().getRol().name())
                        .build();

                ClienteDTO clienteDto = ClienteDTO.newBuilder()
                        .setId(u.getId())
                        .setDni(u.getDni())
                        .setNombre(u.getNombre())
                        .setApellido(u.getApellido())
                        .setEmail(u.getEmail())
                        .setTelefono(u.getTelefono() != null ? u.getTelefono() : "")
                        .setFechaNacimiento(u.getFechaNacimiento() != null ? u.getFechaNacimiento().toString() : "")
                        .setEstaActivo(u.getEstaActivo())
                        .setMetadatos(metaDto)
                        .build();
                        

                responseBuilder.addCliente(clienteDto);
            }


            // Enviamos la respuesta y cerramos el canal
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            // Manejo de error estructurado
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("Error al obtener la lista de usuarios: " + e.getMessage())
                    .asRuntimeException()
            );
        }
    }

    @Override
    public void listarClientes(ListarClientesRequest request, StreamObserver<ListarClientesResponse> responseObserver) {
        try {
            
            List<Usuario> usuarios = usuarioRepository.findAllClientes();
            
            
            ListarClientesResponse.Builder responseBuilder = ListarClientesResponse.newBuilder();

            
            for (Usuario u : usuarios) {
                Metadatos metaDto = Metadatos.newBuilder()
                        .setId(u.getMetadatos().getId())
                        .setUsuario(u.getMetadatos().getUsuario())
                        .setPassword(u.getMetadatos().getPassword())
                        .setRol(u.getMetadatos().getRol().name())
                        .build();

                ClienteDTO clienteDto = ClienteDTO.newBuilder()
                        .setId(u.getId())
                        .setDni(u.getDni())
                        .setNombre(u.getNombre())
                        .setApellido(u.getApellido())
                        .setEmail(u.getEmail())
                        .setTelefono(u.getTelefono() != null ? u.getTelefono() : "")
                        .setFechaNacimiento(u.getFechaNacimiento() != null ? u.getFechaNacimiento().toString() : "")
                        .setEstaActivo(u.getEstaActivo())
                        .setMetadatos(metaDto)
                        .build();
                        
                // Añadimos el cliente a la lista "repeated" de la respuesta
                responseBuilder.addCliente(clienteDto);
            }
            


            // Enviamos la respuesta y cerramos el canal
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            // Manejo de error estructurado
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("Error al obtener la lista de clientes: " + e.getMessage())
                    .asRuntimeException()
            );
        }
    }


    @Override
    public void borrarClienteId(BorrarClienteIdRequest request, StreamObserver<BorrarClienteIdResponse> responseObserver) {
        try {
            // Buscamos el usuario por el ID que viene en la petición gRPC
            Usuario u = usuarioRepository.findById(request.getId());

            if (u != null) {
                u.setEstaActivo(false);
                usuarioRepository.save(u);


                ClienteDTO cliente = mapearUsuarioAResponse(u, u.getMetadatos());
                BorrarClienteIdResponse response = BorrarClienteIdResponse.newBuilder().setCliente(cliente).build();
                responseObserver.onNext(response);
            } else {
                responseObserver.onError(
                Status.NOT_FOUND
                    .withDescription("El usuario con ID " + request.getId() + " no existe.")
                    .asRuntimeException()
                );
            }
        } catch (Exception e) {
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("Error al intentar dar de baja al cliente: " + e.getMessage())
                    .asRuntimeException()
                );
        }
        
        
        responseObserver.onCompleted();
    }

    @Override
    public void crearCliente(CrearClienteRequest request, StreamObserver<CrearClienteResponse> responseObserver) {
       
       try{
            Usuario u = new Usuario();
            u.setDni(request.getDni());
            u.setNombre(request.getNombre());
            u.setApellido(request.getApellido());
            u.setEmail(request.getEmail());
            u.setTelefono(request.getTelefono());
            u.setFechaNacimiento(LocalDate.parse(request.getFechaNacimiento()));
            u.setEstaActivo(true);
            u.setMetadatos(new clientes.entities.UsuarioMetaDatos(
                null,
                request.getMetadatos().getUsuario(),
                BCrypt.hashpw(request.getMetadatos().getPassword(), BCrypt.gensalt()),
                clientes.enums.Rol.CLIENTE,
                null
            ));
            if(usuarioRepository.findByDni(request.getDni()) == null){
                if(usuarioRepository.findByMetadatos_Usuario(request.getMetadatos().getUsuario()) == null){
                    if(usuarioRepository.findByEmail(request.getEmail()) == null){
                        usuarioRepository.save(u);
                        ClienteDTO cliente = mapearUsuarioAResponse(u, u.getMetadatos());
                        CrearClienteResponse response = CrearClienteResponse.newBuilder().setCliente(cliente).build();
                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                    }
                    else{
                        responseObserver.onError(
                            Status.ALREADY_EXISTS
                                    .withDescription("Ya hay un cliente con ese email.")
                                    .asRuntimeException()
                        );
                        return;
                    }
                }
                else{
                    responseObserver.onError(
                        Status.ALREADY_EXISTS
                                .withDescription("Ya hay un cliente con ese nombre de usuario.")
                                .asRuntimeException()
                    );
                    return;
                }
            }else{
                responseObserver.onError(
                        Status.ALREADY_EXISTS
                                .withDescription("Ya hay un cliente con ese DNI.")
                                .asRuntimeException()
                );
            }

       }
       catch(Exception e){
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("Error al crear el cliente: " + e.getMessage())
                    .asRuntimeException()
                );
       }
        
    }

    
    //Modifica solo los campos que no sean nulos segun el usuario pasado por DNI
   @Override
    public void modificarClienteDni(ModificarClienteDniRequest request, StreamObserver<ModificarClienteDniResponse> responseObserver){

        try{
            Usuario usuario = usuarioRepository.findByDni(request.getDni());

            if (usuario == null) {
                responseObserver.onError(
                    Status.NOT_FOUND
                        .withDescription("No se encontró un cliente con el DNI proporcionado.")
                        .asRuntimeException()
                );
                return;
            }

            if (!request.getNombre().isBlank()) {
                usuario.setNombre(request.getNombre());
            }
            if (!request.getApellido().isBlank()) {
                usuario.setApellido(request.getApellido());
            }
            if (!request.getFechaNacimiento().isBlank()) {
                usuario.setFechaNacimiento(LocalDate.parse(request.getFechaNacimiento()));
            }
            if(!request.getEmail().isBlank()) {
                usuario.setEmail(request.getEmail());
            }
            if(!request.getTelefono().isBlank()) {
                usuario.setTelefono(request.getTelefono());
            }
            if(request.getMetadatos().isInitialized()) {
                if (!request.getMetadatos().getUsuario().isBlank()) {
                    usuario.getMetadatos().setUsuario(request.getMetadatos().getUsuario());
                }
                if (!request.getMetadatos().getPassword().isBlank()) {
                    usuario.getMetadatos().setPassword(BCrypt.hashpw(request.getMetadatos().getPassword(), BCrypt.gensalt()));
                }
            }

            if(usuarioRepository.findByDni(usuario.getDni()) == null || usuarioRepository.findByDni(usuario.getDni()).getId().equals(usuario.getId())){
                if(usuarioRepository.findByMetadatos_Usuario(usuario.getMetadatos().getUsuario()) == null || usuarioRepository.findByMetadatos_Usuario(usuario.getMetadatos().getUsuario()).getId().equals(usuario.getId())){
                    if(usuarioRepository.findByEmail(usuario.getEmail()) == null || usuarioRepository.findByEmail(usuario.getEmail()).getId().equals(usuario.getId())){
                        usuarioRepository.save(usuario);
                        ClienteDTO cliente = mapearUsuarioAResponse(usuario, usuario.getMetadatos());
                        ModificarClienteDniResponse response = ModificarClienteDniResponse.newBuilder().setCliente(cliente).build();
                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                    }
                    else{
                        responseObserver.onError(
                            Status.ALREADY_EXISTS
                                    .withDescription("Ya hay un cliente con ese email.")
                                    .asRuntimeException()
                        );
                        return;
                    }
                }
                else{
                    responseObserver.onError(
                        Status.ALREADY_EXISTS
                                .withDescription("Ya hay un cliente con ese nombre de usuario.")
                                .asRuntimeException()
                    );
                    return;
                }
            }else{
                responseObserver.onError(
                        Status.ALREADY_EXISTS
                                .withDescription("Ya hay un cliente con ese DNI.")
                                .asRuntimeException()
                );
            }
        }
        catch(Exception e){
            responseObserver.onError(
                Status.ALREADY_EXISTS
                    .withDescription("Error al modificar el cliente: " + e.getMessage())
                    .asRuntimeException()
                );
        }

    }

    //Modifica solo los campos que no sean nulos segun el usuario pasado por ID
    @Override
    public void modificarClienteId(ModificarClienteIdRequest request, StreamObserver<ModificarClienteIdResponse> responseObserver) {
        try {
            Usuario usuario = usuarioRepository.findById(request.getId());

            if (usuario == null) {
                responseObserver.onError(
                    Status.NOT_FOUND
                        .withDescription("No se encontró un cliente con el ID proporcionado.")
                        .asRuntimeException()
                );
                return;
            }


            if( request.getDni().isBlank() == false) {
                usuario.setDni(request.getDni());
            }
            if (request.getNombre().isBlank() == false) {
                usuario.setNombre(request.getNombre());
            }
            if (request.getApellido().isBlank() == false) {
                usuario.setApellido(request.getApellido());
            }
            if (request.getFechaNacimiento().isBlank() == false) {
                usuario.setFechaNacimiento(LocalDate.parse(request.getFechaNacimiento()));
            }
            if( request.getEmail().isBlank() == false) {
                usuario.setEmail(request.getEmail());
            }
            if( request.getTelefono().isBlank() == false) {
                usuario.setTelefono(request.getTelefono());
            }
            if( request.getMetadatos().isInitialized() == true) {
                if (request.getMetadatos().getUsuario().isBlank() == false) {
                    usuario.getMetadatos().setUsuario(request.getMetadatos().getUsuario());
                }
                if (request.getMetadatos().getPassword().isBlank() == false) {
                    usuario.getMetadatos().setPassword(BCrypt.hashpw(request.getMetadatos().getPassword(), BCrypt.gensalt()));
                }
            }



            if(usuarioRepository.findByDni(usuario.getDni()) == null || usuarioRepository.findByDni(usuario.getDni()).getId().equals(usuario.getId())){
                if(usuarioRepository.findByMetadatos_Usuario(usuario.getMetadatos().getUsuario()) == null || usuarioRepository.findByMetadatos_Usuario(usuario.getMetadatos().getUsuario()).getId().equals(usuario.getId())){
                    if(usuarioRepository.findByEmail(usuario.getEmail()) == null || usuarioRepository.findByEmail(usuario.getEmail()).getId().equals(usuario.getId())){
                        usuarioRepository.save(usuario);
                        ClienteDTO cliente = mapearUsuarioAResponse(usuario, usuario.getMetadatos());
                        ModificarClienteIdResponse response = ModificarClienteIdResponse.newBuilder().setCliente(cliente).build();
                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                    }
                    else{
                        responseObserver.onError(
                            Status.ALREADY_EXISTS
                                    .withDescription("Ya hay un cliente con ese email.")
                                    .asRuntimeException()
                        );
                        return;
                    }
                }
                else{
                    responseObserver.onError(
                        Status.ALREADY_EXISTS
                                .withDescription("Ya hay un cliente con ese nombre de usuario.")
                                .asRuntimeException()
                    );
                    return;
                }
            }else{
                responseObserver.onError(
                        Status.ALREADY_EXISTS
                                .withDescription("Ya hay un cliente con ese DNI.")
                                .asRuntimeException()
                );
            }

        } catch(Exception e){
            responseObserver.onError(
                Status.ALREADY_EXISTS
                    .withDescription("Error al modificar el cliente: " + e.getMessage())
                    .asRuntimeException()
                );
        }
    }


    @Override
    public void consultarClienteId(ConsultarClienteIdRequest request, StreamObserver<ConsultarClienteIdResponse> responseObserver) {
        try {
            //Busqueda en la base de datos
            Usuario usuario = usuarioRepository.findById(request.getId());
            
            if (usuario != null) {
                ClienteDTO cliente = mapearUsuarioAResponse(usuario, usuario.getMetadatos());
                ConsultarClienteIdResponse response = ConsultarClienteIdResponse.newBuilder().setCliente(cliente).build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(
                    Status.NOT_FOUND
                        .withDescription("No se encontró ningún cliente con el ID proporcionado.")
                        .asRuntimeException()
                );
            }
        } catch (Exception e) {
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("Error al buscar el cliente por ID: " + e.getMessage())
                    .asRuntimeException()
            );
        }
    }

    @Override
    public void consultarClienteDni(ConsultarClienteDniRequest request, StreamObserver<ConsultarClienteDniResponse> responseObserver) {
        try {
            //Busqueda en la base de datos
            Usuario usuario = usuarioRepository.findByDni(request.getDni());
            
            if (usuario != null) {
                ClienteDTO cliente = mapearUsuarioAResponse(usuario, usuario.getMetadatos());
                ConsultarClienteDniResponse response = ConsultarClienteDniResponse.newBuilder().setCliente(cliente).build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(
                    Status.NOT_FOUND
                        .withDescription("No se encontró ningún cliente con el DNI proporcionado.")
                        .asRuntimeException()
                );
            }
        } catch (Exception e) {
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("Error al buscar el cliente por DNI: " + e.getMessage())
                    .asRuntimeException()
            );
        }
    }


    @Override
    public void verificarExistenciaDni(VerificarExistenciaDniRequest request, StreamObserver<VerificarExistenciaDniResponse> responseObserver) {
        try {
            // Buscamos al usuario por su DNI
             Usuario usuario = usuarioRepository.findByDni(request.getDni());
            
            
            if (usuario != null) {
                VerificarExistenciaDniResponse response = VerificarExistenciaDniResponse.newBuilder()
                        .setMensaje("El cliente existe.")
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(
                    Status.NOT_FOUND
                        .withDescription("No se encontró ningún cliente con el DNI proporcionado.")
                        .asRuntimeException()
                );
            }
        } catch (Exception e) {
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("Error al verificar la existencia del cliente: " + e.getMessage())
                    .asRuntimeException()
            );
        }
    }

    @Override
    public void verificarClienteActivo(VerificarClienteActivoRequest request, StreamObserver<VerificarClienteActivoResponse> responseObserver) {
        try {
            // Buscamos al usuario por su ID
             Usuario usuario = usuarioRepository.findById(request.getId());
            
            if(usuario == null) {
                responseObserver.onError(
                    Status.NOT_FOUND
                        .withDescription("No se encontró ningún cliente con el ID proporcionado.")
                        .asRuntimeException()
                );
                return;
            }

            if (usuario.getEstaActivo() == true) {
                VerificarClienteActivoResponse response = VerificarClienteActivoResponse.newBuilder()
                        .setMensaje("El cliente está activo.")
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                VerificarClienteActivoResponse response = VerificarClienteActivoResponse.newBuilder()
                        .setMensaje("El cliente no está activo.")
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
            
        } catch (Exception e) {
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("Error al verificar si el cliente está activo: " + e.getMessage())
                    .asRuntimeException()
            );
        }
    }

    @Override
    public void validarLogin(ValidarLoginRequest request, StreamObserver<ValidarLoginResponse> response){
        try {
            if (request.getMetadatos().getUsuario().isBlank() || request.getMetadatos().getPassword().isBlank()) {
                response.onError(
                    Status.INVALID_ARGUMENT
                        .withDescription("El usuario y la contraseña son obligatorios.")
                        .asRuntimeException()
                );
                return;
            }

            Usuario usuario = usuarioRepository.findByMetadatos_Usuario(request.getMetadatos().getUsuario());

            if (usuario == null
                    || usuario.getMetadatos() == null
                    || !BCrypt.checkpw(
                            request.getMetadatos().getPassword(),
                            usuario.getMetadatos().getPassword())) {
                response.onError(
                    Status.UNAUTHENTICATED
                        .withDescription("Usuario o contraseña incorrectos.")
                        .asRuntimeException()
                );
                return;
            }

            if (!Boolean.TRUE.equals(usuario.getEstaActivo())) {
                response.onError(
                    Status.PERMISSION_DENIED
                        .withDescription("El usuario está inactivo.")
                        .asRuntimeException()
                );
                return;
            }

            UsuarioMetaDatos metadatos = usuario.getMetadatos();
            Metadatos loginResponse = Metadatos.newBuilder()
                    .setId(metadatos.getId())
                    .setUsuario(metadatos.getUsuario())
                    .setPassword(metadatos.getPassword())
                    .setRol(metadatos.getRol().name())
                    .build();


            ValidarLoginResponse responseValidacion = ValidarLoginResponse.newBuilder().setMetadatos(loginResponse).build();
            response.onNext(responseValidacion);
            response.onCompleted();
        } catch (Exception e) {
            response.onError(
                Status.INTERNAL
                    .withDescription("Error al validar las credenciales: " + e.getMessage())
                    .asRuntimeException()
            );
        }
    }
        


    private ClienteDTO mapearUsuarioAResponse(Usuario usuario, UsuarioMetaDatos metadatos) {

       
        ClienteDTO clienteDto = ClienteDTO.newBuilder()
                .setId(usuario.getId())
                .setDni(usuario.getDni())
                .setNombre(usuario.getNombre())
                .setApellido(usuario.getApellido())
                .setEmail(usuario.getEmail())
                .setTelefono(usuario.getTelefono() != null ? usuario.getTelefono() : "")
                .setFechaNacimiento(usuario.getFechaNacimiento() != null ? usuario.getFechaNacimiento().toString() : "")
                .setEstaActivo(usuario.getEstaActivo())
                .setMetadatos(Metadatos.newBuilder()
                    .setId(metadatos.getId())
                    .setUsuario(metadatos.getUsuario())
                    .setPassword(metadatos.getPassword())
                    .setRol(metadatos.getRol().name())
                    .build()) 
                .build();

       
        return clienteDto;
    }
}   
