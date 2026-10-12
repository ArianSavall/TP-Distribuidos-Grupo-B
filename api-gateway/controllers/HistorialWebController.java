package unla.tp.tp_distribuidos.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HistorialWebController {

    @GetMapping("/historial")
    public String mostrarHistorial() {
        return "historial";
    }
}