package com.barberia.gestion.Security;

import com.barberia.gestion.Repository.RegistroAccesoRepository;
import com.barberia.gestion.model.RegistroAcceso;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/** Tras un login correcto: guarda datos basicos en la sesion, registra el acceso y redirige segun el rol. */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final RegistroAccesoRepository registroAccesoRepository;

    public LoginSuccessHandler(RegistroAccesoRepository registroAccesoRepository) {
        this.registroAccesoRepository = registroAccesoRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        UsuarioPrincipal usuario = (UsuarioPrincipal) authentication.getPrincipal();
        String rol = usuario.getRol().toLowerCase();

        // La sesion ya fue renovada por Spring Security (proteccion contra session fixation)
        HttpSession session = request.getSession();
        session.setAttribute("rol", rol);
        switch (usuario.getRol()) {
            case "BARBERO" -> {
                session.setAttribute("barberoId", usuario.getId());
                session.setAttribute("barberoNombre", usuario.getNombre());
            }
            case "CLIENTE" -> {
                session.setAttribute("clienteId", usuario.getId());
                session.setAttribute("clienteNombre", usuario.getNombre());
            }
            default -> session.setAttribute("adminNombre", usuario.getNombre());
        }

        registroAccesoRepository.save(new RegistroAcceso(usuario.getUsername(), rol, LocalDateTime.now(), true));

        String destino = switch (usuario.getRol()) {
            case "ADMIN" -> "/clientes";
            case "BARBERO" -> "/cita/pendientes";
            default -> "/menu";
        };
        response.sendRedirect(request.getContextPath() + destino);
    }
}
