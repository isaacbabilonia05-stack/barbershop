package com.barberia.gestion.Security;

import com.barberia.gestion.Repository.RegistroAccesoRepository;
import com.barberia.gestion.model.RegistroAcceso;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Endpoints JWT:
 *  - POST /api/auth/login  {"email": "...", "password": "..."}  ->  devuelve el token
 *  - GET  /api/auth/me     (Authorization: Bearer <token>)       ->  datos del usuario autenticado
 */
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RegistroAccesoRepository registroAccesoRepository;

    public AuthApiController(CustomUserDetailsService userDetailsService, PasswordEncoder passwordEncoder,
                             JwtService jwtService, RegistroAccesoRepository registroAccesoRepository) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.registroAccesoRepository = registroAccesoRepository;
    }

    public record LoginRequest(String email, String password) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest datos) {
        String email = datos == null || datos.email() == null ? "" : datos.email().trim();
        String password = datos == null || datos.password() == null ? "" : datos.password();

        UsuarioPrincipal usuario = null;
        try {
            var encontrado = (UsuarioPrincipal) userDetailsService.loadUserByUsername(email);
            if (encontrado.isEnabled() && passwordEncoder.matches(password, encontrado.getPassword())) {
                usuario = encontrado;
            }
        } catch (UsernameNotFoundException ignorado) {
            // se responde igual que con clave incorrecta (no revelar si el email existe)
        }

        if (usuario == null) {
            registroAccesoRepository.save(new RegistroAcceso(email, "desconocido", LocalDateTime.now(), false));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales invalidas"));
        }

        registroAccesoRepository.save(
                new RegistroAcceso(usuario.getUsername(), usuario.getRol().toLowerCase(), LocalDateTime.now(), true));

        return ResponseEntity.ok(Map.of(
                "token", jwtService.generarToken(usuario),
                "tipo", "Bearer",
                "expiraEnSegundos", jwtService.getExpiracionSegundos(),
                "rol", usuario.getRol()));
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        var usuario = (UsuarioPrincipal) authentication.getPrincipal();
        return Map.of(
                "id", usuario.getId(),
                "nombre", usuario.getNombre(),
                "email", usuario.getUsername(),
                "rol", usuario.getRol());
    }
}
