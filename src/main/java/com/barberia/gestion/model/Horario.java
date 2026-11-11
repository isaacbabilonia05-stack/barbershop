package com.barberia.gestion.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalTime;

@Document(collection = "horarios")
public class Horario {

    @Id
    private Long id;

    private Integer diaSemana;
    private LocalTime horaApertura;
    private LocalTime horaCierre;

    @DBRef
    private Barbero barbero;

    public Horario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getDiaSemana() { return diaSemana; }
    public void setDiaSemana(Integer diaSemana) { this.diaSemana = diaSemana; }

    public LocalTime getHoraApertura() { return horaApertura; }
    public void setHoraApertura(LocalTime horaApertura) { this.horaApertura = horaApertura; }

    public LocalTime getHoraCierre() { return horaCierre; }
    public void setHoraCierre(LocalTime horaCierre) { this.horaCierre = horaCierre; }

    public Barbero getBarbero() { return barbero; }
    public void setBarbero(Barbero barbero) { this.barbero = barbero; }
}
