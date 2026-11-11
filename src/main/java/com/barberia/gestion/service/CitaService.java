package com.barberia.gestion.service;

import com.barberia.gestion.model.*;
import com.barberia.gestion.Repository.CitaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CitaService {

    private final CitaRepository citaRepository;
    private final DisponibilidadService disponibilidadService;

    public CitaService(CitaRepository citaRepository, DisponibilidadService disponibilidadService) {
        this.citaRepository = citaRepository;
        this.disponibilidadService = disponibilidadService;
    }

    public Cita crearCita(Cliente cliente, Barbero barbero, Cubiculo cubiculo,
                          LocalDate fecha, LocalTime horaInicio, List<Servicio> servicios, List<Integer> cantidades) {

        System.out.println("=== CREANDO CITA ===");
        System.out.println("Cliente: " + (cliente != null ? cliente.getNombre() : "N/A"));
        System.out.println("Barbero: " + (barbero != null ? barbero.getNombre() : "N/A"));
        System.out.println("Fecha: " + fecha);
        System.out.println("Hora inicio: " + horaInicio);
        System.out.println("Servicios seleccionados: " + servicios.size());

        // 1. Calcular duración total y costo
        int duracionTotal = 0;
        for (int i = 0; i < servicios.size(); i++) {
            Servicio s = servicios.get(i);
            int cantidad = cantidades.get(i);
            duracionTotal += s.getDuracionMinutos() * cantidad;
        }

        LocalTime horaFin = horaInicio.plusMinutes(duracionTotal);
        System.out.println("Duracion total: " + duracionTotal + " minutos - Hora fin: " + horaFin);

        // 2. Verificar disponibilidad del barbero
        boolean estaDisponible = false;
        try {
            estaDisponible = disponibilidadService.isBarberoDisponible(barbero, fecha, horaInicio, horaFin);
        } catch (Exception e) {
            System.out.println("Advertencia al consultar DisponibilidadService: " + e.getMessage());
        }

        // Si el servicio de disponibilidad da false, verificamos directamente contra las citas en BD
        if (!estaDisponible) {
            if (tieneCitaSuperpuesta(barbero, fecha, horaInicio, horaFin)) {
                throw new RuntimeException("El barbero no está disponible en ese horario");
            }
        }

        // 3. Crear la entidad Cita
        Cita cita = new Cita();
        cita.setCliente(cliente);
        cita.setBarbero(barbero);
        cita.setCubiculo(cubiculo);
        cita.setFecha(fecha);
        cita.setHoraInicio(horaInicio);
        cita.setHoraFin(horaFin);
        cita.setEstado(EstadoCita.PENDIENTE);

        List<DetalleCita> detalles = new ArrayList<>();
        for (int i = 0; i < servicios.size(); i++) {
            Servicio s = servicios.get(i);
            int cantidad = cantidades.get(i);

            DetalleCita detalle = new DetalleCita();
            detalle.setServicio(s);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(s.getPrecio());
            detalle.setSubtotal(s.getPrecio() * cantidad);
            detalles.add(detalle);
        }
        cita.setDetalles(detalles);

        Cita citaGuardada = citaRepository.save(cita);
        System.out.println("=== CITA CREADA CON EXITO CON ID: " + citaGuardada.getId() + " ===");
        return citaGuardada;
    }

    /**
     * Valida directamente si el barbero ya tiene una cita activa que se solape en ese rango.
     */
    private boolean tieneCitaSuperpuesta(Barbero barbero, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        if (barbero == null) return false;
        
        List<Cita> citasExistentes = citaRepository.findByBarberoAndEstado(barbero, EstadoCita.PENDIENTE);
        
        for (Cita c : citasExistentes) {
            if (c.getFecha() != null && c.getFecha().equals(fecha)) {
                LocalTime inicioExistente = c.getHoraInicio();
                LocalTime finExistente = c.getHoraFin();

                if (inicioExistente != null && finExistente != null) {
                    // Condición matemática de solapamiento de rangos de tiempo
                    if (horaInicio.isBefore(finExistente) && horaFin.isAfter(inicioExistente)) {
                        return true; // Existe colisión de horario
                    }
                }
            }
        }
        return false;
    }

    public Cita actualizarEstadoCita(Long citaId, EstadoCita nuevoEstado) {
        Cita cita = citaRepository.findById(citaId).orElseThrow();
        cita.setEstado(nuevoEstado);
        return citaRepository.save(cita);
    }

    public List<Cita> getCitasPendientesPorBarbero(Barbero barbero) {
        return citaRepository.findByBarberoAndEstado(barbero, EstadoCita.PENDIENTE);
    }
}