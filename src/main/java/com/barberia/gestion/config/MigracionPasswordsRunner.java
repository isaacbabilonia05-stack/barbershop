package com.barberia.gestion.config;

import com.barberia.gestion.Repository.BarberoRepository;
import com.barberia.gestion.Repository.ClienteRepository;
import com.barberia.gestion.model.Barbero;
import com.barberia.gestion.model.Cliente;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Encripta una sola vez las contrasenas que quedaron en texto plano
 * (las que venian de MySQL). Es idempotente: si ya estan en BCrypt las ignora.
 *
 * Se activa con app.encriptar-passwords-existentes=true en application.properties.
 * Cuando ya no quede ninguna en texto plano, puedes ponerlo en false o borrar esta clase.
 */
@Component
@ConditionalOnProperty(name = "app.encriptar-passwords-existentes", havingValue = "true")
public class MigracionPasswordsRunner implements CommandLineRunner {

    private final ClienteRepository clienteRepository;
    private final BarberoRepository barberoRepository;
    private final PasswordEncoder passwordEncoder;

    public MigracionPasswordsRunner(ClienteRepository clienteRepository,
                                    BarberoRepository barberoRepository,
                                    PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.barberoRepository = barberoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Una contrasena BCrypt siempre empieza por $2a$, $2b$ o $2y$ y mide 60 caracteres. */
    private boolean yaEstaEncriptada(String password) {
        return password != null && password.length() == 60 && password.startsWith("$2");
    }

    @Override
    public void run(String... args) {
        int clientesActualizados = 0;
        for (Cliente cliente : clienteRepository.findAll()) {
            if (cliente.getPassword() != null && !yaEstaEncriptada(cliente.getPassword())) {
                cliente.setPassword(passwordEncoder.encode(cliente.getPassword()));
                clienteRepository.save(cliente);
                clientesActualizados++;
            }
        }

        int barberosActualizados = 0;
        for (Barbero barbero : barberoRepository.findAll()) {
            if (barbero.getPassword() != null && !yaEstaEncriptada(barbero.getPassword())) {
                barbero.setPassword(passwordEncoder.encode(barbero.getPassword()));
                barberoRepository.save(barbero);
                barberosActualizados++;
            }
        }

        if (clientesActualizados + barberosActualizados > 0) {
            System.out.println("=== Passwords encriptadas: " + clientesActualizados
                    + " clientes y " + barberosActualizados + " barberos ===");
        }
    }
}
