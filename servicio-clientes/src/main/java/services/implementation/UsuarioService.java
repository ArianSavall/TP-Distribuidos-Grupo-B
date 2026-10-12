package services.implementation;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;

import entities.Usuario;
import entities.UsuarioMetaDatos;
import grpc.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import repositories.IUsuarioRepository;


@GrpcService
public class UsuarioService extends ClienteServiceGrpc.ClienteServiceImplBase{


    private IUsuarioRepository usuarioRepository;

    private PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    public UsuarioService(IUsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void listarUsuarios(ListarClientesRequest request, StreamObserver<ListarClientesResponse> responseObserver) {
        try {
            //Logica para obtener la lista de usuarios desde la base de datos
            List<Usuario> usuarios = usuarioRepository.findAllWithMetadatos();
            
             
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
    public void borrarClienteId(ClienteIdRequest request, StreamObserver<ClienteResponse> responseObserver) {
        try {
            // Buscamos el usuario por el ID que viene en la petición gRPC
            Usuario u = usuarioRepository.findById(request.getId());

            if (u != null) {
                u.setEstaActivo(false);
                usuarioRepository.save(u);


                ClienteResponse response = mapearUsuarioAResponse(u, u.getMetadatos());
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
    public void crearCliente(ClienteRequest request, StreamObserver<ClienteResponse> responseObserver) {
       
       try{
            Usuario u = new Usuario();
            u.setDni(request.getDni());
            u.setNombre(request.getNombre());
            u.setApellido(request.getApellido());
            u.setEmail(request.getEmail());
            u.setTelefono(request.getTelefono());
            u.setFechaNacimiento(LocalDate.parse(request.getFechaNacimiento()));
            u.setEstaActivo(true);
            u.setMetadatos(new entities.UsuarioMetaDatos(
                null,
                request.getMetadatos().getUsuario(),
                passwordEncoder().encode(request.getMetadatos().getPassword()),
                enums.Rol.CLIENTE,
                null
            ));

            usuarioRepository.save(u);

            ClienteResponse response = mapearUsuarioAResponse(u, u.getMetadatos());

            responseObserver.onNext(response);
            responseObserver.onCompleted();
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
    public void modificarClienteDni(ClienteRequest request, StreamObserver<ClienteResponse> response){

        try{
            Usuario usuario = usuarioRepository.findByDni(request.getDni());

            if (usuario == null) {
                response.onError(
                    Status.NOT_FOUND
                        .withDescription("No se encontró un usuario con el DNI proporcionado.")
                        .asRuntimeException()
                );
                return;
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
            if(request.getEmail().isBlank() == false) {
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
                    usuario.getMetadatos().setPassword(passwordEncoder().encode(request.getMetadatos().getPassword()));
                }
            }

            usuarioRepository.save(usuario);

            ClienteResponse responseCliente = mapearUsuarioAResponse(usuario, usuario.getMetadatos());
            response.onNext(responseCliente);
            response.onCompleted();
        }
        catch(Exception e){
            response.onError(
                Status.ALREADY_EXISTS
                    .withDescription("Error al modificar el cliente: " + e.getMessage())
                    .asRuntimeException()
                );
        }

    }

    //Modifica solo los campos que no sean nulos segun el usuario pasado por ID
    @Override
    public void modificarClienteId(ModificarClienteIdRequest request, StreamObserver<ClienteResponse> responseObserver) {
        try {
            Usuario usuario = usuarioRepository.findById(request.getId());

            if (usuario == null) {
                responseObserver.onError(
                    Status.NOT_FOUND
                        .withDescription("No se encontró un usuario con el ID proporcionado.")
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
                    usuario.getMetadatos().setPassword(passwordEncoder().encode(request.getMetadatos().getPassword()));
                }
            }

            usuarioRepository.save(usuario);

            ClienteResponse responseCliente = mapearUsuarioAResponse(usuario, usuario.getMetadatos());
            responseObserver.onNext(responseCliente);
            responseObserver.onCompleted();
        } catch(Exception e){
            responseObserver.onError(
                Status.ALREADY_EXISTS
                    .withDescription("Error al modificar el cliente: " + e.getMessage())
                    .asRuntimeException()
                );
        }
    }


    @Override
    public void consultarClienteId(ClienteIdRequest request, StreamObserver<ClienteResponse> responseObserver) {
        try {
            //Busqueda en la base de datos
            Usuario usuario = usuarioRepository.findById(request.getId());
            
            if (usuario != null) {
                ClienteResponse response = mapearUsuarioAResponse(usuario, usuario.getMetadatos());
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
    public void consultarClienteDni(ClienteDniRequest request, StreamObserver<ClienteResponse> responseObserver) {
        try {
            //Busqueda en la base de datos
            Usuario usuario = usuarioRepository.findByDni(request.getDni());
            
            if (usuario != null) {
                ClienteResponse response = mapearUsuarioAResponse(usuario, usuario.getMetadatos());
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
    public void verificarExistenciaDni(ClienteDniRequest request, StreamObserver<VerificarActivoResponse> responseObserver) {
        try {
            // Buscamos al usuario por su DNI
             Usuario usuario = usuarioRepository.findByDni(request.getDni());
            
            
            if (usuario != null) {
                VerificarActivoResponse response = VerificarActivoResponse.newBuilder()
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
    public void verificarClienteActivo(ClienteIdRequest request, StreamObserver<VerificarActivoResponse> responseObserver) {
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
                VerificarActivoResponse response = VerificarActivoResponse.newBuilder()
                        .setMensaje("El cliente está activo.")
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                VerificarActivoResponse response = VerificarActivoResponse.newBuilder()
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
        


    private ClienteResponse mapearUsuarioAResponse(Usuario usuario, UsuarioMetaDatos metadatos) {

       
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

       
        return ClienteResponse.newBuilder()
                .setCliente(clienteDto)
                .build();
    }
}   
