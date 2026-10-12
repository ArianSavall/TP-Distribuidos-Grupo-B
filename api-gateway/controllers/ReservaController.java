package unla.tp.tp_distribuidos.controllers;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import unla.tp.tp_distribuidos.dtos.ReservaDTO;
import unla.tp.tp_distribuidos.dtos.ReservaGraphQLDTO;
import unla.tp.tp_distribuidos.dtos.UsuarioDTO;
import unla.tp.tp_distribuidos.dtos.VehiculoDTO;
import unla.tp.tp_distribuidos.dtos.VehiculoDisponibilidadDTO;
import unla.tp.tp_distribuidos.repositories.IUsuarioRepository;

@Controller 
@RequestMapping("/reservas")
public class ReservaController {
    private final String apiBaseUrl = "http://localhost:8080/api/v1/reservas";
    private final String apiBaseUrlGraph = "http://localhost:8080/graphql";

    private IUsuarioRepository usuarioRepository;

    public ReservaController(IUsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String pantallaReservas(Model model, @CookieValue(name = "JSESSIONID", required = false) String jsessionid,
        @RequestParam(required = false) String fechaInicioDisp,
        @RequestParam(required = false) String fechaFinalDisp,
        Authentication authentication,
        @RequestParam(required = false) String tipoVehiculoDisp,
        @RequestParam(required = false) String marcaDisp,
        @RequestParam(required = false) String modeloDisp,
        @RequestParam(required = false) Double precioMinDisp,
        @RequestParam(required = false) Double precioMaxDisp,
        @RequestParam(required = false) String dniCliente,
        @RequestParam(required = false) String patenteVehiculo,
        @RequestParam(required = false) String tipoVehiculo,
        @RequestParam(required = false) String estado,
        @RequestParam(required = false) String fechaDesde,
        @RequestParam(required = false) String fechaHasta
        ) {


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (jsessionid != null) {
            headers.add("Cookie", "JSESSIONID=" + jsessionid);
        }
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        boolean esAdmin = authentication != null && authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("esAdmin", esAdmin);


        if (fechaInicioDisp != null && !fechaInicioDisp.isEmpty() && 
            fechaFinalDisp != null && !fechaFinalDisp.isEmpty()) {
            try {
                ObjectNode graphqlRequest = mapper.createObjectNode();
                graphqlRequest.put("query", "query($tipo: TipoVehiculo, $marca: String, $modelo: String, "
                        + "$precioMin: Float, $precioMax: Float, $fechaInicio: String!, $fechaFinal: String!) "
                        + "{ vehiculosDisponibles(tipo: $tipo, marca: $marca, modelo: $modelo, "
                        + "precioMin: $precioMin, precioMax: $precioMax, fechaInicio: $fechaInicio, "
                        + "fechaFinal: $fechaFinal) { patente marca modelo anio color tipoVehiculo precioDiario } }");
                ObjectNode variables = graphqlRequest.putObject("variables");
                variables.put("fechaInicio", fechaInicioDisp);
                variables.put("fechaFinal", fechaFinalDisp);
                if (tipoVehiculoDisp == null || tipoVehiculoDisp.isBlank()) variables.putNull("tipo");
                else variables.put("tipo", tipoVehiculoDisp);
                if (marcaDisp == null || marcaDisp.isBlank()) variables.putNull("marca");
                else variables.put("marca", marcaDisp.trim());
                if (modeloDisp == null || modeloDisp.isBlank()) variables.putNull("modelo");
                else variables.put("modelo", modeloDisp.trim());
                if (precioMinDisp == null) variables.putNull("precioMin");
                else variables.put("precioMin", precioMinDisp);
                if (precioMaxDisp == null) variables.putNull("precioMax");
                else variables.put("precioMax", precioMaxDisp);

                HttpEntity<String> entityDisp = new HttpEntity<>(mapper.writeValueAsString(graphqlRequest), headers);
                
                ResponseEntity<String> responseDisp = restTemplate.exchange(
                        "http://localhost:8080/graphql", HttpMethod.POST, entityDisp, String.class);

                JsonNode rootDisp = mapper.readTree(responseDisp.getBody());
                JsonNode vehiculosNode = rootDisp.path("data").path("vehiculosDisponibles");

                List<VehiculoDisponibilidadDTO> vehiculosDisponibles = new ArrayList<>();
                if (!vehiculosNode.isMissingNode() && !vehiculosNode.isNull()) {
                    vehiculosDisponibles = mapper.readValue(vehiculosNode.traverse(), new TypeReference<List<VehiculoDisponibilidadDTO>>() {});
                }

                model.addAttribute("vehiculosDisponibles", vehiculosDisponibles);
                model.addAttribute("fechaInicioBuscada", fechaInicioDisp);
                model.addAttribute("fechaFinalBuscada", fechaFinalDisp);
                model.addAttribute("tipoVehiculoBuscado", tipoVehiculoDisp);
                model.addAttribute("marcaBuscada", marcaDisp);
                model.addAttribute("modeloBuscado", modeloDisp);
                model.addAttribute("precioMinBuscado", precioMinDisp);
                model.addAttribute("precioMaxBuscado", precioMaxDisp);

            } catch (Exception e) {
                model.addAttribute("errorDisponibilidad", "No se pudo consultar la disponibilidad.");
            }
        }

        StringBuilder filtrosGraph = new StringBuilder();

        if (esAdmin && dniCliente != null && !dniCliente.trim().isEmpty()) {
        filtrosGraph.append("dniCliente: \\\"").append(dniCliente.trim()).append("\\\", ");
        }
        if (patenteVehiculo != null && !patenteVehiculo.trim().isEmpty()) {
            filtrosGraph.append("patenteVehiculo: \\\"").append(patenteVehiculo.trim()).append("\\\", ");
        }
        if (tipoVehiculo != null && !tipoVehiculo.trim().isEmpty()) {
            filtrosGraph.append("tipoVehiculo: ").append(tipoVehiculo).append(", "); 
        }
        if (estado != null && !estado.trim().isEmpty()) {
            filtrosGraph.append("estado: ").append(estado).append(", "); 
        }
        if (fechaDesde != null && !fechaDesde.isEmpty()) {
        filtrosGraph.append("fechaDesde: \\\"").append(fechaDesde).append("T00:00:00\\\", ");
        } else if (fechaHasta == null || fechaHasta.isEmpty()) {
            String hoyISO = LocalDateTime.now().with(LocalTime.MIN).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            filtrosGraph.append("fechaDesde: \\\"").append(hoyISO).append("\\\", ");
        }
        
        if (fechaHasta != null && !fechaHasta.isEmpty()) {
            filtrosGraph.append("fechaHasta: \\\"").append(fechaHasta).append("T23:59:59\\\", ");
        }

        String queryGraphQL = 
            """
            {
            "query": "query { reservas(filtros: { %s }) { id cliente vehiculo patente fechaInicio fechaFinalizacion importeTotal estado } }"
            }
            """.formatted(filtrosGraph.toString());
        HttpEntity<String> postEntity = new HttpEntity<>(queryGraphQL, headers);
        

        try {
            
            ResponseEntity<String> response = restTemplate.exchange(
                    apiBaseUrlGraph, HttpMethod.POST, postEntity, String.class
            );

            String jsonRespuesta = response.getBody();

            JsonNode rootNode = mapper.readTree(jsonRespuesta);
            JsonNode reservasNode = rootNode.path("data").path("reservas");

            List<ReservaGraphQLDTO> listaReservas = new ArrayList<>();

            if(!reservasNode.isMissingNode() && !reservasNode.isNull()){
                listaReservas = mapper.readValue(
                    reservasNode.traverse(),
                    new TypeReference<List<ReservaGraphQLDTO>>(){}
                );
            }

            model.addAttribute("reservas", listaReservas);
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
            String errorMsg = e.getResponseBodyAsString();
            redirectAttributes.addFlashAttribute("errorMsg", 
                (errorMsg == null || errorMsg.isBlank()) ? "Error: Verifique que la patente ingresada sea correcta." : errorMsg);
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
        restTemplate.setRequestFactory(new org.springframework.http.client.JdkClientHttpRequestFactory());

        try {
    
            restTemplate.exchange(apiBaseUrl + "/" + id + "/cancelar", HttpMethod.PUT, entity, Void.class);
            redirectAttributes.addFlashAttribute("successMsg", "Reserva cancelada exitosamente.");
        } catch (HttpClientErrorException e) {
            String errorMsg = e.getResponseBodyAsString();
            redirectAttributes.addFlashAttribute("errorMsg", 
                (errorMsg == null || errorMsg.isBlank()) ? "Error al procesar la cancelación." : errorMsg);
        }

        return "redirect:/reservas";
    }
}
