package com.barberia.gestion.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "servicios")
public class Servicio {

    @Id
    private Long id;

    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer duracionMinutos;
    private TipoServicio tipo;

    public enum TipoServicio {
        CORTE, BARBA, TINTE, PEINADO, LAVADO, OTRO
    }

    public Servicio() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
    public Integer getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; }
    public TipoServicio getTipo() { return tipo; }
    public void setTipo(TipoServicio tipo) { this.tipo = tipo; }
}
