package unla.tp.tp_distribuidos.services.implementation;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import unla.tp.tp_distribuidos.dtos.UsuarioDTO;
import unla.tp.tp_distribuidos.models.Usuario;
import unla.tp.tp_distribuidos.repositories.IUsuarioRepository;
import unla.tp.tp_distribuidos.services.IUsuarioService;

@Service("usuarioService")
public class UsuarioService implements IUsuarioService {

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

        Usuario savedUsuario = usuarioRepository.save(modelMapper.map(usuario, Usuario.class));
        return modelMapper.map(savedUsuario, UsuarioDTO.class);
    }

    @Override
    public UsuarioDTO patchByDni(UsuarioDTO cambios) {
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
        if (cambios.getEstaActivo() != null) {
            usuario.setEstaActivo(cambios.getEstaActivo());
        }
        if( cambios.getEmail() != null) {
            usuario.setEmail(cambios.getEmail());
        }
        if( cambios.getTelefono() != null) {
            usuario.setTelefono(cambios.getTelefono());
        }

        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        return modelMapper.map(usuarioActualizado, UsuarioDTO.class);
    }

    @Override
    public UsuarioDTO patchById(Long id, UsuarioDTO cambios) {
        Usuario usuario = usuarioRepository.findById(id);

        if (usuario == null) {
            throw new IllegalArgumentException("No se encontró un usuario con el ID proporcionado.");
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

        if (cambios.getEstaActivo() != null) {
            usuario.setEstaActivo(cambios.getEstaActivo());
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
}
