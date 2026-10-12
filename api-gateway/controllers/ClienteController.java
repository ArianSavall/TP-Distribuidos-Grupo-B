package unla.tp.tp_distribuidos.controllers;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import unla.tp.tp_distribuidos.dtos.UsuarioCreateDTO;
import unla.tp.tp_distribuidos.dtos.UsuarioDTO;
import unla.tp.tp_distribuidos.dtos.UsuarioMetaDatosCreateDTO;

@Controller 
@RequestMapping("/clientes")
public class ClienteController {
    private String apiBaseUrl = "http://localhost:8080/api_rest/v1/";

    @GetMapping
    public String listarClientes(Model model, @RequestParam(value = "dni", required = false) String dni, @CookieValue(name = "JSESSIONID", required = false) String jsessionid) {
        HttpHeaders headers = new HttpHeaders();
        if (jsessionid != null) {
            headers.add("Cookie", "JSESSIONID=" + jsessionid);
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        String url = apiBaseUrl + "clientes";
        if(dni != null && !dni.isEmpty()){
            try {
                ResponseEntity<UsuarioDTO> response = restTemplate.exchange(
                        apiBaseUrl + "clientes/buscar/dni/" + dni,
                        HttpMethod.GET,
                        entity,
                        UsuarioDTO.class
                );

                UsuarioDTO cliente = response.getBody();
                model.addAttribute("dniBuscado", dni);
                if (cliente != null) {
                    model.addAttribute("clientes", List.of(cliente));
                } else {
                    model.addAttribute("clientes", List.of());
                    model.addAttribute("errorMsg", "No se encontró ningún cliente con el DNI proporcionado.");
                }
            } catch (Exception e) {
                model.addAttribute("clientes", List.of());
                model.addAttribute("errorMsg", "Error al buscar el cliente: " + e.getMessage());
                model.addAttribute("dniBuscado", dni);
            }
        }
        else{

            ResponseEntity<List<UsuarioDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<UsuarioDTO>>() {}
            );

            model.addAttribute("clientes", response.getBody());
        }

        UsuarioCreateDTO nuevoCliente = new UsuarioCreateDTO();
        nuevoCliente.setMetadatos(new UsuarioMetaDatosCreateDTO()); 
        model.addAttribute("nuevoCliente", nuevoCliente);
        return "clientes";
    }

    @PostMapping("/alta")
    public String altaCliente(@ModelAttribute UsuarioCreateDTO cliente, @CookieValue(name = "JSESSIONID", required = false) String jsessionid, RedirectAttributes redirectAttributes) {
        HttpHeaders headers = new HttpHeaders();
        if (jsessionid != null) {
            headers.add("Cookie", "JSESSIONID=" + jsessionid);
        }
        
        try{
            HttpEntity<UsuarioCreateDTO> entity = new HttpEntity<>(cliente, headers);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForEntity(apiBaseUrl + "clientes", entity, Void.class);
        }
        catch(HttpStatusCodeException e){
            redirectAttributes.addFlashAttribute("errorMsg", "Error al crear el usuario: " + e.getResponseBodyAsString());
        }
        return "redirect:/clientes";
    }

    @PostMapping("/borrar/{id}")
    public String bajaLogicaCliente(@PathVariable Long id, @CookieValue(name = "JSESSIONID", required = false) String jsessionid) {
        HttpHeaders headers = new HttpHeaders();
        if (jsessionid != null) {
            headers.add("Cookie", "JSESSIONID=" + jsessionid);
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.exchange(apiBaseUrl + "clientes/borrar/id/" + id, HttpMethod.DELETE, entity, Void.class);
        return "redirect:/clientes";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model, @CookieValue(name = "JSESSIONID", required = false) String jsessionid) {
        HttpHeaders headers = new HttpHeaders();
        if (jsessionid != null) {
            headers.add("Cookie", "JSESSIONID=" + jsessionid);
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        UsuarioDTO cliente = restTemplate.exchange(apiBaseUrl + "clientes/buscar/id/" + id, HttpMethod.GET, entity, UsuarioDTO.class).getBody();
        model.addAttribute("cliente", cliente);
        return "clientesUpdate"; 
    }

    @PostMapping("/modificar/{id}")
    public String modificarCliente(@PathVariable Long id, @ModelAttribute UsuarioDTO cliente, @CookieValue(name = "JSESSIONID", required = false) String jsessionid, RedirectAttributes redirectAttributes) {
        HttpHeaders headers = new HttpHeaders();
        if (jsessionid != null) {
            headers.add("Cookie", "JSESSIONID=" + jsessionid);
        }
        try{
            HttpEntity<UsuarioDTO> entity = new HttpEntity<>(cliente, headers);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.setRequestFactory(new JdkClientHttpRequestFactory());
            restTemplate.exchange(apiBaseUrl + "clientes/modificar/id/" + id, HttpMethod.PATCH, entity, Void.class);
            return "redirect:/clientes";
        }
        catch(HttpStatusCodeException e){
            redirectAttributes.addFlashAttribute("errorMsg", "Error al modificar al usuario: " + e.getResponseBodyAsString());
            return "redirect:/clientes/editar/"+ id;
        }
    }
}
