package unla.tp.tp_distribuidos.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter 
@Getter 
@NoArgsConstructor 
@AllArgsConstructor 
public class UsuarioMetaDatosDTO {
    private Long id;
    private String usuario;
    private String password;
    private String rol;

}
