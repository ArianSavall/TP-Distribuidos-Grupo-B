package clientes.repositories;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import clientes.entities.Usuario;

@Repository("usuarioRepository") 
public interface IUsuarioRepository extends JpaRepository<Usuario, Serializable> {

    @EntityGraph(attributePaths = "metadatos")
    @Query("SELECT u FROM Usuario u WHERE u.estaActivo = true")
    List<Usuario> findAllWithMetadatos();
    @EntityGraph(attributePaths = "metadatos")
    public abstract Usuario findById(Long id);

    @EntityGraph(attributePaths = "metadatos")
    public abstract Usuario findByDni(String dni);

    @EntityGraph(attributePaths = "metadatos")
    public abstract Usuario findByEmail(String email);

    @EntityGraph(attributePaths = "metadatos")
    public abstract Usuario findByTelefono(String telefono);

    @EntityGraph(attributePaths = "metadatos")
    @Query("SELECT u FROM Usuario u WHERE u.metadatos.rol = 'CLIENTE' AND u.estaActivo = true")
    public abstract List<Usuario> findAllClientes();

    public abstract Usuario findByMetadatos_Usuario(String usuario);
}
