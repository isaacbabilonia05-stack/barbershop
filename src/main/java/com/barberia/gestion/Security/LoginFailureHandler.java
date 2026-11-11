package com.barberia.gestion.Security;

import com.barberia.gestion.Repository.RegistroAccesoRepository;
import com.barberia.gestion.model.RegistroAcceso;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/** Login fallido: deja constancia en registro_accesos y vuelve al formulario con un mensaje. */
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final RegistroAccesoRepository registroAccesoRepository;

    public LoginFailureHandler(RegistroAccesoRepository registroAccesoRepository) {
        this.registroAccesoRepository = registroAccesoRepository;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String usuario = request.getParameter("username");
        registroAccesoRepository.save(new RegistroAcceso(usuario, null, LocalDateTime.now(), false));

        String parametro = (exception instanceof DisabledException) ? "inactivo" : "error";
        response.sendRedirect(request.getContextPath() + "/login?" + parametro);
    }
}
