package com.barberia.gestion.Controller;

import com.barberia.gestion.Repository.BarberoRepository;
import com.barberia.gestion.Repository.HorarioRepository;
import com.barberia.gestion.model.Barbero;
import com.barberia.gestion.model.Horario;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;

/**
 * CRUD de la coleccion "horarios" para el rol BARBERO: cada barbero administra
 * unicamente SUS horarios (ruta /barbero/** restringida a ROLE_BARBERO).
 */
@Controller
@RequestMapping("/barbero/horarios")
public class BarberoHorarioController {

    private static final String[] DIAS = {"", "Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado", "Domingo"};

    private final HorarioRepository horarioRepository;
    private final BarberoRepository barberoRepository;

    public BarberoHorarioController(HorarioRepository horarioRepository, BarberoRepository barberoRepository) {
        this.horarioRepository = horarioRepository;
        this.barberoRepository = barberoRepository;
    }

    private Barbero barberoActual(HttpSession session) {
        Long id = (Long) session.getAttribute("barberoId");
        if (id == null) throw new AccessDeniedException("Sesion de barbero no valida");
        return barberoRepository.findById(id).orElseThrow();
    }

    @GetMapping
    public String listar(HttpSession session, Model model) {
        Barbero barbero = barberoActual(session);
        List<Horario> horarios = new java.util.ArrayList<>(horarioRepository.findByBarbero(barbero));
        horarios.sort(Comparator.comparing(Horario::getDiaSemana));
        model.addAttribute("horarios", horarios);
        model.addAttribute("dias", DIAS);
        model.addAttribute("nombre", barbero.getNombre());
        return "barbero_horarios";
    }

    /** Crea el horario de un dia o, si ya existe, lo actualiza (uno por dia de la semana). */
    @PostMapping("/guardar")
    public String guardar(@RequestParam Integer diaSemana, @RequestParam String horaApertura,
                          @RequestParam String horaCierre, HttpSession session, RedirectAttributes flash) {
        Barbero barbero = barberoActual(session);
        try {
            LocalTime apertura = LocalTime.parse(horaApertura);
            LocalTime cierre = LocalTime.parse(horaCierre);
            if (diaSemana < 1 || diaSemana > 7) {
                flash.addFlashAttribute("err", "Dia no valido");
            } else if (!apertura.isBefore(cierre)) {
                flash.addFlashAttribute("err", "La hora de apertura debe ser anterior a la de cierre");
            } else {
                List<Horario> existentes = horarioRepository.findByBarberoAndDiaSemana(barbero, diaSemana);
                Horario h = existentes.isEmpty() ? new Horario() : existentes.get(0);
                h.setBarbero(barbero);
                h.setDiaSemana(diaSemana);
                h.setHoraApertura(apertura);
                h.setHoraCierre(cierre);
                horarioRepository.save(h);
                flash.addFlashAttribute("msg", "Horario guardado para " + DIAS[diaSemana]);
            }
        } catch (DateTimeParseException e) {
            flash.addFlashAttribute("err", "Formato de hora no valido");
        }
        return "redirect:/barbero/horarios";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        Barbero barbero = barberoActual(session);
        Horario h = horarioRepository.findById(id).orElseThrow();
        if (h.getBarbero() == null || !barbero.getId().equals(h.getBarbero().getId())) {
            throw new AccessDeniedException("Ese horario no es tuyo");
        }
        horarioRepository.deleteById(id);
        flash.addFlashAttribute("msg", "Horario eliminado");
        return "redirect:/barbero/horarios";
    }
}
