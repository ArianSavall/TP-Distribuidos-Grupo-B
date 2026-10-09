package models;

import enums.Rol;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioMetaDatos {

    private Long id;

    private String usuario;

    private String password;

    private Rol rol;

    private Usuario usuarioPrincipal;
}
