package unla.tp.tp_distribuidos.services.implementation;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import unla.tp.tp_distribuidos.dtos.UsuarioCreateDTO;
import unla.tp.tp_distribuidos.dtos.UsuarioDTO;
import unla.tp.tp_distribuidos.models.Usuario;
import unla.tp.tp_distribuidos.repositories.IUsuarioRepository;
import unla.tp.tp_distribuidos.services.IUsuarioService;

@Service("usuarioService")
public class UsuarioService implements IUsuarioService, UserDetailsService {

    private PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    private IUsuarioRepository usuarioRepository;
    
    private ModelMapper modelMapper = new ModelMapper();

    public UsuarioService(IUsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<Usuario> getAll() {
        return usuarioRepository.findAll();
    }

    @Override 
    public List<Usuario> getAllClientes() {
        return usuarioRepository.findAllClientes();
    }

    @Override
    public boolean remove(long id) {
        try {
            usuarioRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //Cambia su estado a inactivo pero no lo elimina de la base de datos
    @Override
    public boolean bajaCliente(Long id) {
        Usuario usuario = usuarioRepository.findById(id);
        if (usuario != null) {
            usuario.setEstaActivo(false);
            usuarioRepository.save(usuario);
            return true;
        }
        return false;
    }

    //Verifica que el dni, email y telefono no esten repetidos
    //Ademas lo activa y encripta la contraseña antes de guardarlo
    @Override
    public UsuarioDTO insertOrUpdate(UsuarioDTO usuario) {
        if(usuarioRepository.findByDni(usuario.getDni()) != null) {
            throw new IllegalArgumentException("El DNI ya está registrado en la base de datos.");
        }
        else if (usuarioRepository.findByEmail(usuario.getEmail()) != null) {
            throw new IllegalArgumentException("El email ya está registrado en la base de datos.");
        }
        else if (usuarioRepository.findByTelefono(usuario.getTelefono()) != null) {
            throw new IllegalArgumentException("El teléfono ya está registrado en la base de datos.");
        }
        else if (usuarioRepository.findByMetadatos_Usuario(usuario.getMetadatos().getUsuario()) != null) {
            throw new IllegalArgumentException("El nombre de usuario ya está registrado en la base de datos.");
        }
        usuario.setEstaActivo(true);
        usuario.getMetadatos().setPassword(passwordEncoder().encode(usuario.getMetadatos().getPassword()));
        Usuario savedUsuario = usuarioRepository.save(modelMapper.map(usuario, Usuario.class));
        return modelMapper.map(savedUsuario, UsuarioDTO.class);
    }

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

        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        return modelMapper.map(usuarioActualizado, UsuarioDTO.class);
    }

    //Modifica solo los campos que no sean nulos segun el usuario pasado por ID
    @Override
    public UsuarioDTO patchById(Long id, UsuarioCreateDTO cambios) {
        Usuario usuario = usuarioRepository.findById(id);

        if (usuario == null) {
            throw new IllegalArgumentException("No se encontró un usuario con el ID proporcionado.");
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

        Usuario actualizado = usuarioRepository.save(usuario);
        return modelMapper.map(actualizado, UsuarioDTO.class);
    }

    @Override
    public UsuarioDTO getById(Long id) {
        Usuario usuario = usuarioRepository.findById(id);
        return modelMapper.map(usuario, UsuarioDTO.class);
    }

    @Override
    public UsuarioDTO getByDni(String dni) {
        Usuario usuario = usuarioRepository.findByDni(dni);
        return modelMapper.map(usuario, UsuarioDTO.class);
    }


    //Seccion de Spring Security para la autenticacion de usuarios

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByMetadatos_Usuario(username);

        if (usuario == null) {
            throw new UsernameNotFoundException("Usuario no encontrado");
        }


        return User.builder()
                .username(usuario.getMetadatos().getUsuario())
                .password(usuario.getMetadatos().getPassword())
                .roles(usuario.getMetadatos().getRol().name())
                .disabled(!Boolean.TRUE.equals(usuario.getEstaActivo()))
                .build();
    }
}
