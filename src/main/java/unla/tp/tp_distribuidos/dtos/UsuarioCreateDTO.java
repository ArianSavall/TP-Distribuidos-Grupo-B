package unla.tp.tp_distribuidos.dtos;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class UsuarioCreateDTO {

    private String dni;

    private String nombre;

    private String apellido;

    private String email;

    private String telefono;

    private LocalDate fechaNacimiento;

    private UsuarioMetaDatosCreateDTO metadatos;
}
