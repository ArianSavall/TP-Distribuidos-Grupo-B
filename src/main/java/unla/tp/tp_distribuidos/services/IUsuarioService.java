package unla.tp.tp_distribuidos.services;

import java.util.List;

import unla.tp.tp_distribuidos.dtos.UsuarioCreateDTO;
import unla.tp.tp_distribuidos.dtos.UsuarioDTO;
import unla.tp.tp_distribuidos.models.Usuario;

public interface IUsuarioService {
    public List<Usuario> getAll();
    
    public List<Usuario> getAllClientes();

    public boolean remove(long id);

    public boolean bajaCliente(Long id);

    public UsuarioDTO insertOrUpdate(UsuarioDTO usuario);

    public UsuarioDTO patchByDni(UsuarioCreateDTO cambios); //Modificar sin pasar todo el usuario completo

    public UsuarioDTO patchById(Long id, UsuarioCreateDTO cambios); //Modificar sin pasar todo el usuario completo

    public UsuarioDTO getById(Long id);

    public UsuarioDTO getByDni(String dni);


}
