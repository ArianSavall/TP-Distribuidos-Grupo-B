package unla.tp.tp_distribuidos.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unla.tp.tp_distribuidos.models.Usuario;

@Repository
public interface IUsuarioRepository extends JpaRepository<Usuario, Long> {}