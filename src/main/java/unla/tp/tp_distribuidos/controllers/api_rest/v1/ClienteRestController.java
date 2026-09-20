package unla.tp.tp_distribuidos.controllers.api_rest.v1;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import unla.tp.tp_distribuidos.dtos.UsuarioCreateDTO;
import unla.tp.tp_distribuidos.dtos.UsuarioDTO;
import unla.tp.tp_distribuidos.enums.Rol;
import unla.tp.tp_distribuidos.services.IUsuarioService;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;




@RestController 
@RequestMapping("/api_rest/v1/clientes")
@Tag(name = "Clientes", description = "Operaciones relacionadas con los clientes")
public class ClienteRestController {

    private IUsuarioService usuarioService;

    private ModelMapper modelMapper = new ModelMapper();

    public ClienteRestController(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation (summary = "Listar todos los clientes")
    public ResponseEntity<List<UsuarioDTO>> getClientes() {
        List<UsuarioDTO> clientes = usuarioService.getAllClientes().stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioDTO.class))
                .toList();
        return new ResponseEntity<List<UsuarioDTO>>(clientes, HttpStatus.OK);
    }

    @GetMapping("/buscar/id/{id}")
    @Operation(summary = "Obtener cliente por ID")
    public ResponseEntity<UsuarioDTO> getCliente(@PathVariable Long id) {
        UsuarioDTO usuario = usuarioService.getById(id);
        if (usuario != null) {
            UsuarioDTO clienteDTO = modelMapper.map(usuario, UsuarioDTO.class);
            return ResponseEntity.ok(clienteDTO);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/buscar/dni/{dni}")
    @Operation(summary = "Obtener cliente por DNI")
    public ResponseEntity<UsuarioDTO> getClienteByDni(@PathVariable String dni) {
        UsuarioDTO usuario = usuarioService.getByDni(dni);
        if (usuario != null) {
            UsuarioDTO clienteDTO = modelMapper.map(usuario, UsuarioDTO.class);
            return ResponseEntity.ok(clienteDTO);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping
    @Operation(summary = "Alta cliente")
    public ResponseEntity<UsuarioDTO> crearCliente(@RequestBody UsuarioCreateDTO usuarioNuevo){
        UsuarioDTO usuario = modelMapper.map(usuarioNuevo, UsuarioDTO.class);

        usuario.getMetadatos().setRol(Rol.CLIENTE.toString());

        UsuarioDTO nuevo = usuarioService.insertOrUpdate(usuario);
        return ResponseEntity.ok(nuevo);
    }

    @DeleteMapping("/borrar/id/{id}")
    @Operation(summary = "Baja logica cliente por ID")
    public ResponseEntity<String> bajaLogicaClienteById(@PathVariable Long id) {
        boolean eliminado = usuarioService.bajaCliente(id);
        return eliminado ? ResponseEntity.ok("Cliente dado de baja correctamente") : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente no encontrado");
    }

    @DeleteMapping("/borrar/dni/{dni}")
    @Operation(summary = "Baja logica cliente por DNI")
    public ResponseEntity<String> bajaLogicaClienteByDni(@PathVariable String dni) {
        UsuarioDTO usuario = usuarioService.getByDni(dni);

        if(usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente no encontrado");
        }

        boolean eliminado = usuarioService.bajaCliente(usuario.getId());
        return eliminado ? ResponseEntity.ok("Cliente dado de baja correctamente") : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente no encontrado");
    }


    @PatchMapping("/modificar/id/{id}")
    @Operation(summary = "Modificar cliente por ID")
    public ResponseEntity<UsuarioDTO> modificarCliente(@PathVariable Long id, @RequestBody UsuarioCreateDTO usuario) {

        UsuarioDTO actualizado = usuarioService.patchById(id, usuario);

        if (actualizado == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(actualizado);
    }

    @PatchMapping("/modificar/dni")
    @Operation(summary = "Modificar cliente por DNI")
    public ResponseEntity<UsuarioDTO> modificarClienteByDni(@RequestBody UsuarioCreateDTO usuario) {

        if (usuario.getDni() == null || usuario.getDni().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        UsuarioDTO existente = usuarioService.getByDni(usuario.getDni());

        if (existente == null) {
            return ResponseEntity.notFound().build();
        }


        UsuarioDTO actualizado = usuarioService.patchByDni(usuario);

        if(actualizado == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(actualizado);
    }
}