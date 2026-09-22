package unla.tp.tp_distribuidos.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter 
@Getter 
@NoArgsConstructor 
@AllArgsConstructor 
public class UsuarioMetaDatosCreateDTO {

    private String usuario;

    private String password;
}
