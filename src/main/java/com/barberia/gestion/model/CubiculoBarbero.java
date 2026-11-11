package com.barberia.gestion.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "cubiculo_barbero")
public class CubiculoBarbero {

    @Id
    private Long id;

    private LocalDate fechaAsignacion;
    private LocalDate fechaFin;
    private Boolean activo;

    @DBRef
    private Cubiculo cubiculo;

    @DBRef
    private Barbero barbero;

    public CubiculoBarbero() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(LocalDate fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public Cubiculo getCubiculo() { return cubiculo; }
    public void setCubiculo(Cubiculo cubiculo) { this.cubiculo = cubiculo; }
    public Barbero getBarbero() { return barbero; }
    public void setBarbero(Barbero barbero) { this.barbero = barbero; }
}
