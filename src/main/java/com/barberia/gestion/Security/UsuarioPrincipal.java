package com.barberia.gestion.Security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Usuario autenticado. Une los tres tipos de usuario (admin, barbero, cliente)
 * que viven en colecciones distintas de MongoDB.
 */
public class UsuarioPrincipal implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String nombre;
    private final String email;
    private final String password;
    /** ADMIN, BARBERO o CLIENTE (sin el prefijo ROLE_). */
    private final String rol;
    private final boolean activo;

    public UsuarioPrincipal(Long id, String nombre, String email, String password, String rol, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.activo = activo;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getRol() { return rol; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return email; }
    @Override public boolean isEnabled() { return activo; }

    // equals/hashCode por email: el control de sesiones concurrentes usa el principal como clave
    @Override
    public boolean equals(Object o) {
        return o instanceof UsuarioPrincipal other && Objects.equals(email, other.email);
    }

    @Override
    public int hashCode() { return Objects.hashCode(email); }
}
