package com.barberia.gestion.config;

import com.barberia.gestion.Repository.AdministradorRepository;
import com.barberia.gestion.model.Administrador;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea el administrador inicial en MongoDB si la coleccion esta vacia.
 * Las credenciales salen de application.properties (app.admin.*), no estan quemadas en el codigo
 * y la contrasena se guarda con BCrypt.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private final AdministradorRepository administradorRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;
    private final String nombre;

    public AdminSeeder(AdministradorRepository administradorRepository, PasswordEncoder passwordEncoder,
                       @Value("${app.admin.email}") String email,
                       @Value("${app.admin.password}") String password,
                       @Value("${app.admin.nombre:Administrador}") String nombre) {
        this.administradorRepository = administradorRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
        this.nombre = nombre;
    }

    @Override
    public void run(String... args) {
        if (administradorRepository.count() == 0) {
            administradorRepository.save(new Administrador(nombre, email, passwordEncoder.encode(password)));
            System.out.println("=== Administrador inicial creado: " + email + " ===");
        }
    }
}
