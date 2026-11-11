package com.barberia.gestion.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** Usuario con rol ADMIN. Vive en su propia coleccion "administradores" (nada de credenciales en el codigo). */
@Document(collection = "administradores")
public class Administrador {

    @Id
    private Long id;

    private String nombre;

    @Indexed(unique = true)
    private String email;

    /** Siempre guardada con BCrypt. */
    private String password;

    private Boolean activo = true;

    public Administrador() {}

    public Administrador(String nombre, String email, String password) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.activo = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
