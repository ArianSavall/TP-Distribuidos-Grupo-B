package unla.tp.tp_distribuidos.controllers;

import java.util.Collections;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import unla.tp.tp_distribuidos.dtos.PatenteDTO;
import unla.tp.tp_distribuidos.dtos.VehiculoDTO;
import unla.tp.tp_distribuidos.dtos.VehiculoUpdateDTO;

@Controller 
@RequestMapping("/vehiculos")
public class VehiculoController {
    private final String apiBaseUrl = "http://localhost:8080/api_rest/v1/vehiculos";

    @GetMapping
    public String listarVehiculos(
            @RequestParam(value = "patente", required = false) String patente,
            Model model,
            @CookieValue(name = "JSESSIONID", required = false) String jsessionid) {

        HttpHeaders headers = new HttpHeaders();
        if (jsessionid != null) headers.add("Cookie", "JSESSIONID=" + jsessionid);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        if (patente != null && !patente.trim().isEmpty()) {
            try {
                ResponseEntity<VehiculoDTO> response = restTemplate.exchange(
                        apiBaseUrl + "/" + patente.trim(),
                        HttpMethod.GET,
                        requestEntity,
                        VehiculoDTO.class
                );
                model.addAttribute("vehiculos", response.getBody() != null ? List.of(response.getBody()) : Collections.emptyList());
                model.addAttribute("patenteBuscada", patente);
            } catch (HttpClientErrorException.NotFound e) {
                model.addAttribute("vehiculos", Collections.emptyList());
                model.addAttribute("mensajeBusqueda", "No se encontró vehículo con patente: " + patente);
                model.addAttribute("patenteBuscada", patente);
            }
        } else {

            ResponseEntity<List<VehiculoDTO>> response = restTemplate.exchange(
                    apiBaseUrl + "/getAll",
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<List<VehiculoDTO>>() {}
            );
            model.addAttribute("vehiculos", response.getBody());
        }

        model.addAttribute("nuevoVehiculo", new VehiculoDTO());
        return "vehiculos";
    }

    @PostMapping("/alta")
    public String altaVehiculo(@ModelAttribute VehiculoDTO vehiculo,
            @CookieValue(name = "JSESSIONID", required = false) String jsessionid,
            RedirectAttributes redirectAttributes) {
        HttpHeaders headers = new HttpHeaders();
        if (jsessionid != null) headers.add("Cookie", "JSESSIONID=" + jsessionid);
        
        vehiculo.setEstaActivo(true); // Se asume activo al crear[cite: 27]
        HttpEntity<VehiculoDTO> entity = new HttpEntity<>(vehiculo, headers);
        RestTemplate restTemplate = new RestTemplate();
        
        try {
            restTemplate.exchange(apiBaseUrl + "/create", HttpMethod.POST, entity, VehiculoDTO.class);
        } catch (HttpClientErrorException.Conflict e) {
            redirectAttributes.addFlashAttribute("mensajeAlta", "Error: La patente ya está registrada");
        }

        return "redirect:/vehiculos";
    }

    @PostMapping("/estado/{patente}")
    public String cambiarEstado(
            @PathVariable String patente, 
            @RequestParam boolean activar, 
            @CookieValue(name = "JSESSIONID", required = false) String jsessionid) {
        
        HttpHeaders headers = new HttpHeaders();
        if (jsessionid != null) headers.add("Cookie", "JSESSIONID=" + jsessionid);
        
        PatenteDTO patenteDTO = new PatenteDTO(patente);
        HttpEntity<PatenteDTO> entity = new HttpEntity<>(patenteDTO, headers);
        RestTemplate restTemplate = new RestTemplate();
        
        String endpoint = activar ? "/activar" : "/desactivar"; 
        restTemplate.exchange(apiBaseUrl + endpoint, HttpMethod.POST, entity, VehiculoDTO.class);
        
        return "redirect:/vehiculos";
    }

    @GetMapping("/editar/{patente}")
    public String mostrarFormularioEdicion(
            @PathVariable String patente, 
            Model model, 
            @CookieValue(name = "JSESSIONID", required = false) String jsessionid) {
        
        HttpHeaders headers = new HttpHeaders();
        if (jsessionid != null) headers.add("Cookie", "JSESSIONID=" + jsessionid);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<VehiculoDTO> response = restTemplate.exchange(
                apiBaseUrl + "/" + patente, HttpMethod.GET, requestEntity, VehiculoDTO.class
        );
        
        model.addAttribute("vehiculo", response.getBody());
        return "vehiculosUpdate";
    }

    @PostMapping("/modificar/{patente}")
    public String modificarVehiculo(
            @PathVariable String patente, 
            @ModelAttribute VehiculoUpdateDTO vehiculoDTO, 
            @CookieValue(name = "JSESSIONID", required = false) String jsessionid) {
        
        HttpHeaders headers = new HttpHeaders();
        if (jsessionid != null) headers.add("Cookie", "JSESSIONID=" + jsessionid);
        HttpEntity<VehiculoUpdateDTO> entity = new HttpEntity<>(vehiculoDTO, headers);
        
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new org.springframework.http.client.JdkClientHttpRequestFactory());
        
        restTemplate.exchange(apiBaseUrl + "/" + patente, HttpMethod.PUT, entity, VehiculoDTO.class);
        return "redirect:/vehiculos";
    }
}
