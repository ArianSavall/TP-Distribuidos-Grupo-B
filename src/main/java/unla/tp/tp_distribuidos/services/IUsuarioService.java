package unla.tp.tp_distribuidos.services;

import java.util.List;

import unla.tp.tp_distribuidos.dtos.UsuarioDTO;
import unla.tp.tp_distribuidos.models.Usuario;

public interface IUsuarioService {
    public List<Usuario> getAll();
    
    public List<Usuario> getAllClientes();

    public boolean remove(long id);

    public UsuarioDTO insertOrUpdate(UsuarioDTO usuario);

    public UsuarioDTO patchByDni(UsuarioDTO cambios);

    public UsuarioDTO patchById(Long id, UsuarioDTO cambios);

    public UsuarioDTO getById(Long id);

    public UsuarioDTO getByDni(String dni);


}
