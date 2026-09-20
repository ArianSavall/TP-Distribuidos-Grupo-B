package unla.tp.tp_distribuidos.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import unla.tp.tp_distribuidos.dtos.ReservaDTO;
import unla.tp.tp_distribuidos.dtos.UsuarioDTO;
import unla.tp.tp_distribuidos.dtos.VehiculoDTO;
import unla.tp.tp_distribuidos.repositories.IUsuarioRepository;

@Controller 
@RequestMapping("/reservas")
public class ReservaController {
    private final String apiBaseUrl = "http://localhost:8080/api/v1/reservas"; 

    private IUsuarioRepository usuarioRepository;

    public ReservaController(IUsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String pantallaReservas(Model model, @CookieValue(name = "JSESSIONID", required = false) String jsessionid) {
        HttpHeaders headers = new HttpHeaders();
        if (jsessionid != null) headers.add("Cookie", "JSESSIONID=" + jsessionid);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Asumiendo que tienes un GET /api/v1/reservas para listar (o puedes adaptar a tu GraphQL si aplica)
            ResponseEntity<List<ReservaDTO>> response = restTemplate.exchange(
                    apiBaseUrl, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<List<ReservaDTO>>() {}
            );
            model.addAttribute("reservas", response.getBody());
        } catch (Exception e) {
            model.addAttribute("errorListado", "No se pudieron cargar las reservas.");
        }

        if (!model.containsAttribute("nuevaReserva")) {
            model.addAttribute("nuevaReserva", new ReservaDTO());
        }
        return "reservas";
    }


    @PostMapping("/alta")
    public String altaReserva( 
            @RequestParam("patenteVehiculo") String patenteVehiculo,
            @RequestParam("fechaHoraInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHoraInicio,
            @RequestParam("fechaHoraFinal") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHoraFinal,
            @CookieValue(name = "JSESSIONID", required = false) String jsessionid,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
            
        HttpHeaders headers = new HttpHeaders();
        if (jsessionid != null) headers.add("Cookie", "JSESSIONID=" + jsessionid);
        
        RestTemplate restTemplate = new RestTemplate();

        try {
            HttpEntity<Void> getEntity = new HttpEntity<>(headers);

            String dniCliente = usuarioRepository.findByMetadatos_Usuario(authentication.getName()).getDni(); 

            

            ResponseEntity<UsuarioDTO> clienteRes = restTemplate.exchange(
                "http://localhost:8080/api_rest/v1/clientes/buscar/dni/" + dniCliente,
                 HttpMethod.GET, getEntity, UsuarioDTO.class);

            ResponseEntity<VehiculoDTO> vehiculoRes = restTemplate.exchange(
                "http://localhost:8080/api_rest/v1/vehiculos/" + patenteVehiculo,
                HttpMethod.GET, getEntity, VehiculoDTO.class);

            ReservaDTO nuevaReserva = new ReservaDTO();
            nuevaReserva.setIdCliente(clienteRes.getBody().getId());
            nuevaReserva.setIdVehiculo(vehiculoRes.getBody().getId());
            nuevaReserva.setFechaHoraInicio(fechaHoraInicio);
            nuevaReserva.setFechaHoraFinal(fechaHoraFinal);

            HttpEntity<ReservaDTO> entity = new HttpEntity<>(nuevaReserva,headers);
            restTemplate.exchange(apiBaseUrl, HttpMethod.POST, entity, Void.class);
            redirectAttributes.addFlashAttribute("successMsg", "Reserva creada exitosamente.");
        } catch (HttpClientErrorException e) {
            // Captura el mensaje de error de tu backend (ej. "El vehículo no existe")
            redirectAttributes.addFlashAttribute("errorMsg", "Error al crear: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Error inesperado al crear la reserva: " + e.getMessage());
        }
        
        return "redirect:/reservas";
    }


    @PostMapping("/cancelar/{id}")
    public String cancelarReserva(
            @PathVariable Long id, 
            @CookieValue(name = "JSESSIONID", required = false) String jsessionid,
            RedirectAttributes redirectAttributes) {
            
        HttpHeaders headers = new HttpHeaders();
        if (jsessionid != null) headers.add("Cookie", "JSESSIONID=" + jsessionid);
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        RestTemplate restTemplate = new RestTemplate();
        // Usamos JdkClientHttpRequestFactory para soportar el método PUT nativamente
        restTemplate.setRequestFactory(new org.springframework.http.client.JdkClientHttpRequestFactory());

        try {
    
            restTemplate.exchange(apiBaseUrl + "/" + id + "/cancelar", HttpMethod.PUT, entity, Void.class);
            redirectAttributes.addFlashAttribute("successMsg", "Reserva cancelada exitosamente.");
        } catch (HttpClientErrorException e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Error al cancelar: " + e.getResponseBodyAsString());
        }

        return "redirect:/reservas";
    }
}
