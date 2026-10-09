package models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    private Long id;

    private String dni;

    private String nombre;

    private String apellido;

    private String email;

    private String telefono;

    private LocalDate fechaNacimiento;

    private Boolean estaActivo;

    private UsuarioMetaDatos metadatos;

    //private List<Reserva> reservas = new ArrayList<>();
}