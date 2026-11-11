package com.barberia.gestion.Controller;

import com.barberia.gestion.model.*;
import com.barberia.gestion.Repository.*;
import com.barberia.gestion.service.CitaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/cita")
public class CitaController {

    private final CitaRepository citaRepository;
    private final BarberoRepository barberoRepository;
    private final CitaService citaService;

    public CitaController(CitaRepository citaRepository, BarberoRepository barberoRepository,
                          CitaService citaService) {
        this.citaRepository = citaRepository;
        this.barberoRepository = barberoRepository;
        this.citaService = citaService;
    }

    @GetMapping("/pendientes")
    public String verCitasPendientes(HttpSession session, Model model) {
        Long barberoId = (Long) session.getAttribute("barberoId");
        Barbero barbero = barberoRepository.findById(barberoId).orElseThrow();
        
        List<Cita> citasPendientes = citaRepository.findByBarberoAndEstado(barbero, EstadoCita.PENDIENTE);
        List<Cita> citasAceptadas = citaRepository.findByBarberoAndEstado(barbero, EstadoCita.ACEPTADA);
        
        model.addAttribute("citasPendientes", citasPendientes);
        model.addAttribute("citasAceptadas", citasAceptadas);
        model.addAttribute("nombre", session.getAttribute("barberoNombre"));
        return "citas_pendientes";
    }
    
    @PostMapping("/aceptar/{id}")
    public String aceptarCita(@PathVariable Long id, HttpSession session) {
        cambiarEstado(id, session, EstadoCita.PENDIENTE, EstadoCita.ACEPTADA);
        return "redirect:/cita/pendientes";
    }

    @PostMapping("/rechazar/{id}")
    public String rechazarCita(@PathVariable Long id, HttpSession session) {
        cambiarEstado(id, session, EstadoCita.PENDIENTE, EstadoCita.RECHAZADA);
        return "redirect:/cita/pendientes";
    }

    @PostMapping("/finalizar/{id}")
    public String finalizarCita(@PathVariable Long id, HttpSession session) {
        cambiarEstado(id, session, EstadoCita.ACEPTADA, EstadoCita.COMPLETADA);
        return "redirect:/cita/pendientes";
    }

    /** Un barbero solo puede cambiar el estado de SUS citas y solo siguiendo el flujo valido. */
    private void cambiarEstado(Long citaId, HttpSession session, EstadoCita estadoRequerido, EstadoCita nuevoEstado) {
        Long barberoId = (Long) session.getAttribute("barberoId");
        Cita cita = citaRepository.findById(citaId).orElseThrow();
        if (barberoId == null || cita.getBarbero() == null || !barberoId.equals(cita.getBarbero().getId())) {
            throw new AccessDeniedException("Esta cita no pertenece a tu agenda");
        }
        if (cita.getEstado() == estadoRequerido) {
            citaService.actualizarEstadoCita(citaId, nuevoEstado);
        }
    }
}
