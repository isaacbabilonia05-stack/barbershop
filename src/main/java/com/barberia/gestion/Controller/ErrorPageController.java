package com.barberia.gestion.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/** Destino de accessDeniedPage("/acceso-denegado"): usuario logueado sin el rol necesario. */
@Controller
public class ErrorPageController {

    @GetMapping("/acceso-denegado")
    public ModelAndView accesoDenegado() {
        return GlobalExceptionHandler.vista(403, "Acceso denegado",
                "Tu rol no tiene permiso para entrar a esta seccion.");
    }
}
