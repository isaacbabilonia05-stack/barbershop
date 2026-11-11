package com.barberia.gestion.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "barberos")
public class Barbero {

    @Id
    private Long id;

    private String nombre;
    private String especialidad;
    private String telefono;

    @Indexed(unique = true)
    private String email;

    private String password;
    private Boolean activo = true;
    private String imagenUrl;

    public Barbero() {}

    public Barbero(String nombre, String especialidad, String telefono, String email, String password) {
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.telefono = telefono;
        this.email = email;
        this.password = password;
        this.activo = true;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
}
