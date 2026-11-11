package com.barberia.gestion.service;

import com.barberia.gestion.model.Barbero;
import com.barberia.gestion.model.Cita;
import com.barberia.gestion.model.Horario;
import com.barberia.gestion.Repository.CitaRepository;
import com.barberia.gestion.Repository.HorarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DisponibilidadService {

    private final CitaRepository citaRepository;
    private final HorarioRepository horarioRepository;

    public DisponibilidadService(CitaRepository citaRepository, HorarioRepository horarioRepository) {
        this.citaRepository = citaRepository;
        this.horarioRepository = horarioRepository;
    }

    public boolean isBarberoDisponible(Barbero barbero, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        int diaSemana = fecha.getDayOfWeek().getValue();
        List<Horario> horarios = horarioRepository.findByBarberoAndDiaSemana(barbero, diaSemana);
        
        if (horarios.isEmpty()) {
            return false;
        }
        
        Horario horario = horarios.get(0);
        
        if (horaInicio.isBefore(horario.getHoraApertura()) || horaFin.isAfter(horario.getHoraCierre())) {
            return false;
        }
        
        List<Cita> citasExistentes = citaRepository.findByBarberoAndFecha(barbero, fecha);
        
        for (Cita cita : citasExistentes) {
            if (horaInicio.equals(cita.getHoraInicio())) {
                return false;
            }
            if (horaInicio.isBefore(cita.getHoraFin()) && horaFin.isAfter(cita.getHoraInicio())) {
                return false;
            }
        }
        
        return true;
    }

    public List<LocalTime> getHorasDisponibles(Barbero barbero, LocalDate fecha, int duracionMinutos) {
        List<LocalTime> horasDisponibles = new ArrayList<>();
        
        int diaSemana = fecha.getDayOfWeek().getValue();
        System.out.println("=== getHorasDisponibles ===");
        System.out.println("Barbero: " + barbero.getNombre());
        System.out.println("Fecha: " + fecha);
        System.out.println("Día semana: " + diaSemana);
        
        List<Horario> horarios = horarioRepository.findByBarberoAndDiaSemana(barbero, diaSemana);
        System.out.println("Horarios encontrados: " + horarios.size());
        
        if (horarios.isEmpty()) {
            System.out.println("No hay horarios, usando horas por defecto 8am-7pm");
            for (int h = 8; h < 19; h++) {
                horasDisponibles.add(LocalTime.of(h, 0));
                horasDisponibles.add(LocalTime.of(h, 30));
            }
            return horasDisponibles;
        }
        
        Horario horario = horarios.get(0);
        LocalTime inicio = horario.getHoraApertura();
        LocalTime fin = horario.getHoraCierre();
        
        System.out.println("Horario laboral: " + inicio + " - " + fin);
        
        for (LocalTime hora = inicio; hora.isBefore(fin); hora = hora.plusMinutes(30)) {
            horasDisponibles.add(hora);
        }
        
        System.out.println("Total horas disponibles: " + horasDisponibles.size());
        return horasDisponibles;
    }
}