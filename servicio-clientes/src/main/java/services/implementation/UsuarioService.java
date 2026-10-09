package implementation;

import java.util.List;
import grpc.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;


@GrpcService
public class UsuarioService extends ClienteServiceGrpc.ClienteServiceImplBase{


    private void enviarRespuestaDeError(StreamObserver<ClienteResponse> responseObserver, String mensaje) {
        ClienteResponse errorResponse = ClienteResponse.newBuilder()
                .setStatus("ERROR")
                .setMessage(mensaje)
                .build();
        
        responseObserver.onNext(errorResponse);
        responseObserver.onCompleted();
    }


    @Override
    public void listarUsuarios(ListarClientesRequest request, StreamObserver<ListarClientesResponse> responseObserver) {
        try {
            //Logica para obtener la lista de usuarios desde la base de datos
            //List<Usuario> usuarios = usuarioRepository.findAll();
            
            /* 
            ListarClientesResponse.Builder responseBuilder = ListarClientesResponse.newBuilder()
                    .setStatus("SUCCESS")
                    .setMessage("Usuarios obtenidos correctamente.");

            
            for (Usuario u : usuarios) {
                Metadatos metaDto = Metadatos.newBuilder()
                        .setId(u.getMetadatos().getId())
                        .setUsuario(u.getMetadatos().getUsuario())
                        .build();

                ClienteDTO clienteDto = ClienteDTO.newBuilder()
                        .setId(u.getId())
                        .setDni(u.getDni())
                        .setNombre(u.getNombre())
                        .setApellido(u.getApellido())
                        .setEmail(u.getEmail())
                        .setTelefono(u.getTelefono())
                        .setFechaNacimiento(u.getFechaNacimiento() != null ? u.getFechaNacimiento().toString() : "")
                        .setEstaActivo(u.isEstaActivo())
                        .setMetadatos(metaDto)
                        .build();
                        

                responseBuilder.addCliente(clienteDto);
            }*/

            
            //Instancias de ejemplo de ClienteDTO para simular la respuesta
            ClienteDTO c1 = ClienteDTO.newBuilder()
                    .setId(1)
                    .setDni("12345678")
                    .setNombre("Juan")
                    .setApellido("Perez")
                    .setEmail("juan.perez@example.com")
                    .setTelefono("123456789")
                    .setFechaNacimiento("1990-01-01")
                    .setEstaActivo(true)
                    .setMetadatos(Metadatos.newBuilder()
                            .setId(1)
                            .setUsuario("juanp")
                            .setPassword("hashed_password1")
                            .setRol("CLIENTE")
                            .build())
                    .build();
            
            ClienteDTO c2= ClienteDTO.newBuilder()
                    .setId(2)
                    .setDni("87654321")
                    .setNombre("Pedro")
                    .setApellido("Sanchez")
                    .setEmail("pedro.sanchez@example.com")
                    .setTelefono("987654321")
                    .setFechaNacimiento("1986-11-09")
                    .setEstaActivo(true)
                    .setMetadatos(Metadatos.newBuilder()
                            .setId(2)
                            .setUsuario("pedros")
                            .setPassword("hashed_password2")
                            .setRol("CLIENTE")
                            .build())
                    .build();

            ListarClientesResponse responseBuilder = ListarClientesResponse.newBuilder()
                    .setStatus("SUCCESS")
                    .setMessage("Clientes obtenidos correctamente.")
                    .addCliente(c1)
                    .addCliente(c2)
                    .build();

            // Enviamos la respuesta y cerramos el canal
            responseObserver.onNext(responseBuilder);
            responseObserver.onCompleted();

        } catch (Exception e) {
            // Manejo de error estructurado
            ListarClientesResponse errorResponse = ListarClientesResponse.newBuilder()
                    .setStatus("ERROR")
                    .setMessage("Error al obtener la lista de clientes: " + e.getMessage())
                    .build();
            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void listarClientes(ListarClientesRequest request, StreamObserver<ListarClientesResponse> responseObserver) {
        try {
            
            /* 
            //List<Usuario> usuarios = usuarioRepository.findAllClientes();
            
            
            ListarClientesResponse.Builder responseBuilder = ListarClientesResponse.newBuilder()
                    .setStatus("SUCCESS")
                    .setMessage("Clientes obtenidos correctamente.");

            
            for (Usuario u : usuarios) {
                Metadatos metaDto = Metadatos.newBuilder()
                        .setId(u.getMetadatos().getId())
                        .setUsuario(u.getMetadatos().getUsuario())
                        .build();

                ClienteDTO clienteDto = ClienteDTO.newBuilder()
                        .setId(u.getId())
                        .setDni(u.getDni())
                        .setNombre(u.getNombre())
                        .setApellido(u.getApellido())
                        .setEmail(u.getEmail())
                        .setTelefono(u.getTelefono())
                        .setFechaNacimiento(u.getFechaNacimiento() != null ? u.getFechaNacimiento().toString() : "")
                        .setEstaActivo(u.isEstaActivo())
                        .setMetadatos(metaDto)
                        .build();
                        
                // Añadimos el cliente a la lista "repeated" de la respuesta
                responseBuilder.addCliente(clienteDto);
            }
            */

            //Instancias de ejemplo de ClienteDTO para simular la respuesta
            Metadatos metadatos1 = Metadatos.newBuilder()
                            .setId(1)
                            .setUsuario("juanp")
                            .setPassword("hashed_password1")
                            .setRol("CLIENTE")
                            .build();

            Metadatos metadatos2= Metadatos.newBuilder()
                            .setId(2)
                            .setUsuario("pedros")
                            .setPassword("hashed_password2")
                            .setRol("CLIENTE")
                            .build();

            ClienteDTO c1 = ClienteDTO.newBuilder()
                    .setId(1)
                    .setDni("12345678")
                    .setNombre("Juan")
                    .setApellido("Perez")
                    .setEmail("juan.perez@example.com")
                    .setTelefono("123456789")
                    .setFechaNacimiento("1990-01-01")
                    .setEstaActivo(true)
                    .setMetadatos(metadatos1)
                    .build();
            
            ClienteDTO c2= ClienteDTO.newBuilder()
                    .setId(2)
                    .setDni("87654321")
                    .setNombre("Pedro")
                    .setApellido("Sanchez")
                    .setEmail("pedro.sanchez@example.com")
                    .setTelefono("987654321")
                    .setFechaNacimiento("1986-11-09")
                    .setEstaActivo(true)
                    .setMetadatos(metadatos2)
                    .build();


            ListarClientesResponse responseBuilder = ListarClientesResponse.newBuilder()
                    .setStatus("SUCCESS")
                    .setMessage("Clientes obtenidos correctamente.")
                    .addCliente(c1)
                    .addCliente(c2)
                    .build();

            // Enviamos la respuesta y cerramos el canal
            responseObserver.onNext(responseBuilder);
            responseObserver.onCompleted();

        } catch (Exception e) {
            // Manejo de error estructurado
            ListarClientesResponse errorResponse = ListarClientesResponse.newBuilder()
                    .setStatus("ERROR")
                    .setMessage("Error al obtener la lista de clientes: " + e.getMessage())
                    .build();
            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }


    @Override
    public void borrarClienteId(ClienteIdRequest request, StreamObserver<ClienteResponse> responseObserver) {
        try {
            // Buscamos el usuario por el ID que viene en la petición gRPC
            //Usuario usuario = usuarioRepository.findById(request.getId());

            // Simulación de búsqueda de usuario por ID
            Metadatos metadatos = Metadatos.newBuilder()
                    .setId(1)
                    .setUsuario("juanp")
                    .setPassword("hashed_password1")
                    .setRol("CLIENTE")
                    .build();

           ClienteDTO usuario = ClienteDTO.newBuilder()
                    .setId(1)
                    .setDni("12345678")
                    .setNombre("Juan")
                    .setApellido("Perez")
                    .setEmail("juan.perez@example.com")
                    .setTelefono("123456789")
                    .setFechaNacimiento("1990-01-01")
                    .setEstaActivo(true)
                    .setMetadatos(metadatos)
                    .build();

            if (usuario != null) {
                usuario = usuario.toBuilder().setEstaActivo(false).build();
                // usuarioRepository.save(usuario);
                
                ClienteResponse response = ClienteResponse.newBuilder()
                        .setStatus("SUCCESS")
                        .setMessage("Cliente dado de baja exitosamente.")
                        .build();

            responseObserver.onNext(response);
            } else {
                enviarRespuestaDeError(responseObserver, "No se encontró un cliente con el ID proporcionado.");
            }
        } catch (Exception e) {
            enviarRespuestaDeError(responseObserver, "Error interno al intentar dar de baja al cliente: " + e.getMessage());
        }
        
        
        responseObserver.onCompleted();
    }


    //Verifica que el dni, email y telefono no esten repetidos
    //Ademas lo activa y encripta la contraseña antes de guardarlo

    @Override
    public void crearCliente(ClienteRequest request, StreamObserver<ClienteResponse> responseObserver) {
       
       try{
            //1: Validaciones (Hay que conectar la base de datos)
            //Posible ejemplo
            /*if (usuarioRepository.findByDni(request.getDni()) != null) {
                enviarRespuestaDeError(responseObserver, "El DNI ya está registrado en la base de datos.");
                return; 
            }
            if (usuarioRepository.findByEmail(request.getEmail()) != null) {
                enviarRespuestaDeError(responseObserver, "El email ya está registrado en la base de datos.");
                return;
            }
            if (usuarioRepository.findByTelefono(request.getTelefono()) != null) {
                enviarRespuestaDeError(responseObserver, "El teléfono ya está registrado en la base de datos.");
                return;
            }
            if (usuarioRepository.findByMetadatos_Usuario(request.getMetadatos().getUsuario()) != null) {
                enviarRespuestaDeError(responseObserver, "El nombre de usuario ya está registrado en la base de datos.");
                return;
            } */


            Metadatos metadatos = Metadatos.newBuilder()
            .setUsuario(request.getMetadatos().getUsuario())
            .setPassword(request.getMetadatos().getPassword())
            .setRol("CLIENTE")
            .build();

            ClienteResponse response = ClienteResponse.newBuilder()
                    .setStatus("SUCCESS")
                    .setMessage("Cliente creado exitosamente")
                    .setCliente(ClienteDTO.newBuilder()
                        .setId(166L)
                        .setDni(request.getDni())
                        .setNombre(request.getNombre())
                        .setApellido(request.getApellido())
                        .setEmail(request.getEmail())
                        .setTelefono(request.getTelefono())
                        .setFechaNacimiento(request.getFechaNacimiento())
                        .setEstaActivo(true)
                        .setMetadatos(metadatos)
                    )
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
       }
       catch(Exception e){
            enviarRespuestaDeError(responseObserver, "Error al crear el cliente: " + e.getMessage());
       }
        
    }

    /* 
    //Modifica solo los campos que no sean nulos segun el usuario pasado por DNI
    @Override
    public UsuarioDTO patchByDni(UsuarioCreateDTO cambios) {
        
        Usuario usuario = usuarioRepository.findByDni(cambios.getDni());
        if (usuario == null) {
            throw new IllegalArgumentException("No se encontró un usuario con el DNI proporcionado.");
        }

        if (cambios.getNombre() != null) {
            usuario.setNombre(cambios.getNombre());
        }
        if (cambios.getApellido() != null) {
            usuario.setApellido(cambios.getApellido());
        }
        if (cambios.getFechaNacimiento() != null) {
            usuario.setFechaNacimiento(cambios.getFechaNacimiento());
        }
        if( cambios.getEmail() != null) {
            usuario.setEmail(cambios.getEmail());
        }
        if( cambios.getTelefono() != null) {
            usuario.setTelefono(cambios.getTelefono());
        }
        if( cambios.getMetadatos() != null) {
            if (cambios.getMetadatos().getUsuario() != null) {
                usuario.getMetadatos().setUsuario(cambios.getMetadatos().getUsuario());
            }
            if (cambios.getMetadatos().getPassword() != null) {
                usuario.getMetadatos().setPassword(passwordEncoder().encode(cambios.getMetadatos().getPassword()));
            }
        }

        UsuarioDTO usuarioDto = modelMapper.map(usuario, UsuarioDTO.class);

        return insertOrUpdate(usuarioDto);
    }

    //Modifica solo los campos que no sean nulos segun el usuario pasado por ID
    @Override
    public UsuarioDTO patchById(Long id, UsuarioCreateDTO cambios) {
        Usuario usuario = usuarioRepository.findById(id);

        if (usuario == null) {
            throw new IllegalArgumentException("No se encontró un usuario con el ID proporcionado.");
        }

        if(usuario.equals(usuarioRepository.findByDni(cambios.getDni()))){
            modelMapper.map(cambios, usuario);
            return modelMapper.map(usuarioRepository.save(usuario), UsuarioDTO.class);
        }


        if(cambios.getDni() != null) {
            usuario.setDni(cambios.getDni());
        }

        if (cambios.getNombre() != null) {
            usuario.setNombre(cambios.getNombre());
        }

        if (cambios.getApellido() != null) {
            usuario.setApellido(cambios.getApellido());
        }

        if (cambios.getEmail() != null) {
            usuario.setEmail(cambios.getEmail());
        }

        if (cambios.getTelefono() != null) {
            usuario.setTelefono(cambios.getTelefono());
        }

        if (cambios.getFechaNacimiento() != null) {
            usuario.setFechaNacimiento(cambios.getFechaNacimiento());
        }

        if (cambios.getMetadatos() != null) {
            if (cambios.getMetadatos().getUsuario() != null) {
                usuario.getMetadatos().setUsuario(cambios.getMetadatos().getUsuario());
            }
            if (cambios.getMetadatos().getPassword() != null) {
                usuario.getMetadatos().setPassword(passwordEncoder().encode(cambios.getMetadatos().getPassword()));
            }
        }

        UsuarioDTO usuarioDto = modelMapper.map(usuario, UsuarioDTO.class);

        return insertOrUpdate(usuarioDto);
    }
        */


    @Override
    public void consultarClienteId(ClienteIdRequest request, StreamObserver<ClienteResponse> responseObserver) {
        try {
            //Busqueda en la base de datos
            //Usuario usuario = usuarioRepository.findById(request.getId());
            
            //Simulación de búsqueda de usuario por ID
            ClienteDTO usuario = ClienteDTO.newBuilder()
                    .setId(1)
                    .setDni("12345678")
                    .setNombre("Juan")
                    .setApellido("Perez")
                    .setEmail("juan.perez@example.com")
                    .setTelefono("123456789")
                    .setFechaNacimiento("1990-01-01")
                    .setEstaActivo(true)
                    .setMetadatos(Metadatos.newBuilder()
                            .setId(1)
                            .setUsuario("juanp")
                            .setPassword("hashed_password1")
                            .setRol("CLIENTE")
                            .build())
                    .build();

            // 2. Verificamos si existe y armamos la respuesta
            if (usuario != null) {
                ClienteResponse response = mapearUsuarioAResponse(usuario, usuario.getMetadatos(), "Cliente encontrado exitosamente.");
                responseObserver.onNext(response);
            } else {
                enviarRespuestaDeError(responseObserver, "No se encontró ningún cliente con el ID proporcionado.");
            }
        } catch (Exception e) {
            enviarRespuestaDeError(responseObserver, "Error al buscar el cliente por ID: " + e.getMessage());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void consultarClienteDni(ClienteDniRequest request, StreamObserver<ClienteResponse> responseObserver) {
        try {
            //Busqueda en la base de datos
            //Usuario usuario = usuarioRepository.findByDni(request.getDni());

            //Simulación de búsqueda de usuario por DNI
            ClienteDTO usuario = ClienteDTO.newBuilder()
                    .setId(1)
                    .setDni("12345678")
                    .setNombre("Juan")
                    .setApellido("Perez")
                    .setEmail("juan.perez@example.com")
                    .setTelefono("123456789")
                    .setFechaNacimiento("1990-01-01")
                    .setEstaActivo(true)
                    .setMetadatos(Metadatos.newBuilder()
                            .setId(1)
                            .setUsuario("juanp")
                            .setPassword("hashed_password1")
                            .setRol("CLIENTE")
                            .build())
                    .build();
            
            if (usuario != null) {
                ClienteResponse response = mapearUsuarioAResponse(usuario, usuario.getMetadatos(), "Cliente encontrado exitosamente.");
                responseObserver.onNext(response);
            } else {
                enviarRespuestaDeError(responseObserver, "No se encontró ningún cliente con el DNI proporcionado.");
            }
        } catch (Exception e) {
            enviarRespuestaDeError(responseObserver, "Error al buscar el cliente por DNI: " + e.getMessage());
        }
        responseObserver.onCompleted();
    }

    private ClienteResponse mapearUsuarioAResponse(ClienteDTO usuario, Metadatos metadatos, String mensajeExito) {
        
        Metadatos metaDto = Metadatos.newBuilder()
                .setId(metadatos.getId())
                .setUsuario(metadatos.getUsuario())
                .setRol(metadatos.getRol())
                .build();

       
        ClienteDTO clienteDto = ClienteDTO.newBuilder()
                .setId(usuario.getId())
                .setDni(usuario.getDni())
                .setNombre(usuario.getNombre())
                .setApellido(usuario.getApellido())
                .setEmail(usuario.getEmail())
                .setTelefono(usuario.getTelefono())
                .setFechaNacimiento(usuario.getFechaNacimiento())
                .setEstaActivo(usuario.getEstaActivo())
                .setMetadatos(metaDto) 
                .build();

       
        return ClienteResponse.newBuilder()
                .setStatus("SUCCESS")
                .setMessage(mensajeExito)
                .setCliente(clienteDto)
                .build();
    }

    /* 
    @Override
    public void verificarExistenciaDni(ClienteDniRequest request, StreamObserver<ValidacionResponse> responseObserver) {
        try {
            // Buscamos al usuario por su DNI
            // Usuario usuario = usuarioRepository.findByDni(request.getDni());
            
            
            boolean existe = (usuario != null);
            String mensaje = existe ? "El cliente existe en el sistema." : "No se encontró ningún cliente con ese DNI.";
            
            
            ValidacionResponse response = ValidacionResponse.newBuilder()
                    .setEsValido(existe)
                    .setMensaje(mensaje)
                    .build();
                    
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            
            ValidacionResponse errorResponse = ValidacionResponse.newBuilder()
                    .setEsValido(false)
                    .setMensaje("Error interno al verificar la existencia: " + e.getMessage())
                    .build();
            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void verificarClienteActivo(ClienteIdRequest request, StreamObserver<ValidacionResponse> responseObserver) {
        try {
            // Buscamos al usuario por su ID
            // Usuario usuario = usuarioRepository.findById(request.getId());
            
            boolean estaActivo = false;
            String mensaje;
            
            // Verificamos primero si existe, y luego si su estado activo es verdadero
            if (usuario == null) {
                mensaje = "El cliente no existe en el sistema.";
            } else if (usuario.isEstaActivo()) {
                estaActivo = true;
                mensaje = "El cliente se encuentra activo y habilitado para operar.";
            } else {
                mensaje = "El cliente existe, pero se encuentra inactivo/dado de baja.";
            }
            
            // Construimos la respuesta
            ValidacionResponse response = ValidacionResponse.newBuilder()
                    .setEsValido(estaActivo)
                    .setMensaje(mensaje)
                    .build();
                    
            // Enviamos la respuesta y completamos la llamada
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            // En caso de error, bloqueamos la operación devolviendo false
            ValidacionResponse errorResponse = ValidacionResponse.newBuilder()
                    .setEsValido(false)
                    .setMensaje("Error interno al verificar el estado del cliente: " + e.getMessage())
                    .build();
            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }
        */

}   
