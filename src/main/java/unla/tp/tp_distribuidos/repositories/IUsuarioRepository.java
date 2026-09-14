package unla.tp.tp_distribuidos.repositories;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import unla.tp.tp_distribuidos.models.Usuario;

@Repository("usuarioRepository") 
public interface IUsuarioRepository extends JpaRepository<Usuario, Serializable> {
    public abstract Usuario findById(Long id);

    public abstract Usuario findByDni(String dni);

    public abstract Usuario findByEmail(String email);

    public abstract Usuario findByTelefono(String telefono);

    @Query("SELECT u FROM Usuario u WHERE u.metadatos.rol = 'CLIENTE'")
    public abstract List<Usuario> findAllClientes();
}
