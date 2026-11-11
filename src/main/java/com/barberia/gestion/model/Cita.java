package com.barberia.gestion.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "citas")
public class Cita {

    @Id
    private Long id;

    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;

    private EstadoCita estado;
    private Integer calificacion;

    @DBRef
    private Cliente cliente;

    @DBRef
    private Barbero barbero;

    @DBRef
    private Cubiculo cubiculo;

    /** Los detalles van embebidos dentro del documento de la cita. */
    private List<DetalleCita> detalles = new ArrayList<>();

    public Cita() {}

    public void calcularHoraFin() {
        int duracionTotal = detalles.stream()
            .mapToInt(d -> d.getServicio().getDuracionMinutos() * d.getCantidad())
            .sum();
        if (horaInicio != null) {
            this.horaFin = this.horaInicio.plusMinutes(duracionTotal);
        }
    }

    public Double getTotal() {
        if (detalles == null || detalles.isEmpty()) {
            return 0.0;
        }
        return detalles.stream()
            .mapToDouble(d -> d.getSubtotal() == null ? 0.0 : d.getSubtotal())
            .sum();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public EstadoCita getEstado() { return estado; }
    public void setEstado(EstadoCita estado) { this.estado = estado; }

    public Integer getCalificacion() { return calificacion; }
    public void setCalificacion(Integer calificacion) { this.calificacion = calificacion; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Barbero getBarbero() { return barbero; }
    public void setBarbero(Barbero barbero) { this.barbero = barbero; }

    public Cubiculo getCubiculo() { return cubiculo; }
    public void setCubiculo(Cubiculo cubiculo) { this.cubiculo = cubiculo; }

    public List<DetalleCita> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleCita> detalles) { this.detalles = detalles; }
}
