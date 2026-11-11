package com.barberia.gestion.config;

import com.barberia.gestion.Repository.ClienteRepository;
import com.barberia.gestion.model.Cliente;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Crea clientes de prueba (fake) para pruebas de carga / rendimiento.
 *
 * Se activa con app.seed-clientes-fake=N en application.properties (N = cantidad).
 * Es idempotente: si ya existen (mismo patron de email) no vuelve a insertarlos.
 * Todos usan la misma contrasena (app.seed-clientes-fake.password) guardada con BCrypt;
 * se encripta una sola vez porque BCrypt es lento a proposito.
 */
@Component
@ConditionalOnProperty(name = "app.seed-clientes-fake")
public class ClientesFakeSeeder implements CommandLineRunner {

    private static final int LOTE = 1000;
    private static final String DOMINIO = "@fake.barbershop.test";

    private static final String[] NOMBRES = {"Juan", "Carlos", "Andres", "Luis", "Miguel", "Santiago", "Camilo",
            "Daniel", "Felipe", "Sebastian", "David", "Jorge", "Maria", "Laura", "Valentina", "Camila", "Daniela",
            "Sofia", "Paula", "Natalia"};
    private static final String[] APELLIDOS = {"Garcia", "Rodriguez", "Martinez", "Lopez", "Gonzalez", "Perez",
            "Sanchez", "Ramirez", "Torres", "Diaz", "Vargas", "Castro", "Rojas", "Moreno", "Jimenez", "Ortiz"};

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final int cantidad;
    private final String password;

    public ClientesFakeSeeder(ClienteRepository clienteRepository, PasswordEncoder passwordEncoder,
                              @Value("${app.seed-clientes-fake}") int cantidad,
                              @Value("${app.seed-clientes-fake.password:Fake1234}") String password) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.cantidad = cantidad;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (cantidad <= 0) {
            return;
        }

        long inicio = System.currentTimeMillis();

        // Una sola consulta: emails fake que ya existen (asi no hacemos 22000 consultas)
        Set<String> existentes = new HashSet<>();
        for (Cliente c : clienteRepository.findAll()) {
            if (c.getEmail() != null && c.getEmail().endsWith(DOMINIO)) {
                existentes.add(c.getEmail());
            }
        }

        String hash = passwordEncoder.encode(password);
        Random random = new Random(42);
        int insertados = 0;

        for (int desde = 1; desde <= cantidad; desde += LOTE) {
            int hasta = Math.min(desde + LOTE - 1, cantidad);
            List<Cliente> lote = new ArrayList<>(LOTE);
            for (int i = desde; i <= hasta; i++) {
                // siempre consumimos el random para que los nombres sean los mismos en cada arranque
                String nombre = NOMBRES[random.nextInt(NOMBRES.length)] + " "
                        + APELLIDOS[random.nextInt(APELLIDOS.length)] + " "
                        + APELLIDOS[random.nextInt(APELLIDOS.length)];
                if (existentes.contains(email(i))) continue;
                lote.add(new Cliente(nombre, email(i), telefono(i), hash));
            }
            if (!lote.isEmpty()) {
                clienteRepository.saveAll(lote);
                insertados += lote.size();
            }
        }

        System.out.println("=== Clientes fake insertados: " + insertados + " (ya existian "
                + existentes.size() + ") en " + (System.currentTimeMillis() - inicio) + " ms ===");
    }

    private String email(int i) { return "cliente" + i + DOMINIO; }

    /** 10 digitos que empiezan por 9 (no chocan con telefonos reales colombianos que empiezan por 3). */
    private String telefono(int i) { return String.format("9%09d", i); }
}