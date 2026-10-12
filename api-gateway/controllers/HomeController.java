package unla.tp.tp_distribuidos.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;



@Controller
@RequestMapping("/")
public class HomeController {
    
    @GetMapping
    public String index() {
        return "index";
    }
    
    
}