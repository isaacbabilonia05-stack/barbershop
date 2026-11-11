package com.barberia.gestion.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "registro_accesos")
public class RegistroAcceso {

    @Id
    private Long id;

    private String usuario;
    private String rol;
    private LocalDateTime fechaHora;
    private boolean exitoso;

    public RegistroAcceso() {}

    public RegistroAcceso(String usuario, String rol, LocalDateTime fechaHora, boolean exitoso) {
        this.usuario = usuario;
        this.rol = rol;
        this.fechaHora = fechaHora;
        this.exitoso = exitoso;
    }

    public Long getId() { return id; }
    public String getUsuario() { return usuario; }
    public String getRol() { return rol; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public boolean isExitoso() { return exitoso; }

    public void setId(Long id) { this.id = id; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public void setRol(String rol) { this.rol = rol; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public void setExitoso(boolean exitoso) { this.exitoso = exitoso; }
}
