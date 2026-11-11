package com.barberia.gestion.Security;

import com.barberia.gestion.Repository.ClienteRepository;
import com.barberia.gestion.model.Cliente;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Se ejecuta cuando el login con Google fue exitoso. Aqui se juntan
 * OAuth (Google) + aplicacion + JWT + Spring Security:
 *
 *  1. Recibe los datos del usuario de Google (email, nombre).
 *  2. Lo busca en MongoDB (admin, barbero o cliente); si no existe, lo crea como CLIENTE.
 *  3. Le asigna el rol (el que ya tiene en la BD; nunca viene de Google).
 *  4. Genera el JWT y lo guarda en la cookie JWT_TOKEN.
 *  5. Lo autentica en Spring (sesion) con el mismo UsuarioPrincipal del login por formulario
 *     y delega en LoginSuccessHandler, que registra el acceso y redirige segun el rol.
 */
@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final CustomUserDetailsService userDetailsService;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginSuccessHandler loginSuccessHandler;

    private final SecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();

    public OAuth2AuthenticationSuccessHandler(CustomUserDetailsService userDetailsService,
                                              ClienteRepository clienteRepository,
                                              PasswordEncoder passwordEncoder,
                                              JwtService jwtService,
                                              LoginSuccessHandler loginSuccessHandler) {
        this.userDetailsService = userDetailsService;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginSuccessHandler = loginSuccessHandler;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        if (!(authentication.getPrincipal() instanceof OAuth2User googleUser)) {
            rechazar(request, response, "oauth_error");
            return;
        }

        // 1. Datos que entrega Google
        String email = googleUser.getAttribute("email");
        Object verificado = googleUser.getAttribute("email_verified");
        if (email == null || email.isBlank() || !Boolean.parseBoolean(String.valueOf(verificado))) {
            // Solo se acepta un correo que Google haya verificado
            rechazar(request, response, "oauth_error");
            return;
        }
        email = email.trim();
        String nombre = googleUser.getAttribute("name");
        String sub = googleUser.getAttribute("sub");

        // 2 y 3. Buscar al usuario en Mongo (conserva su rol) o crearlo como CLIENTE
        UserDetails usuario;
        try {
            usuario = userDetailsService.loadUserByUsername(email);
        } catch (UsernameNotFoundException noExiste) {
            usuario = crearCliente(email, nombre, sub);
        }

        if (!usuario.isEnabled()) {
            rechazar(request, response, "inactivo");
            return;
        }

        // 4. Generar el JWT y guardarlo en la cookie JWT_TOKEN
        String token = jwtService.generarToken((UsuarioPrincipal) usuario);
        ResponseCookie cookie = ResponseCookie.from(JwtAuthFilter.COOKIE_JWT, token)
                .httpOnly(true)
                .secure(request.isSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(jwtService.getExpiracionSegundos())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // 5. Autenticar en Spring con nuestro UsuarioPrincipal (reemplaza al usuario de Google)
        var auth = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(auth);
        SecurityContextHolder.setContext(contexto);
        contextRepository.saveContext(contexto, request, response);

        // Datos en sesion + registro de acceso + redireccion segun el rol
        loginSuccessHandler.onAuthenticationSuccess(request, response, auth);
    }

    /** Crea un CLIENTE nuevo con los datos de Google. El rol NUNCA lo decide Google. */
    private UserDetails crearCliente(String email, String nombre, String sub) {
        String nombreFinal = (nombre == null || nombre.isBlank()) ? email.substring(0, email.indexOf('@')) : nombre;
        // "telefono" es unico en Mongo y Google no lo entrega: se usa un valor unico que el cliente puede cambiar en /perfil
        String telefono = "google-" + (sub != null ? sub : UUID.randomUUID().toString());
        // Clave aleatoria: esta cuenta entra con Google, no con contrasena
        String claveAleatoria = passwordEncoder.encode(UUID.randomUUID().toString());
        try {
            clienteRepository.save(new Cliente(nombreFinal, email, telefono, claveAleatoria));
        } catch (DuplicateKeyException yaCreado) {
            // otra peticion lo creo al mismo tiempo: se usa el que ya existe
        }
        return userDetailsService.loadUserByUsername(email);
    }

    /** Cancela el login: sin sesion autenticada y de vuelta a /login con un mensaje. */
    private void rechazar(HttpServletRequest request, HttpServletResponse response, String parametro)
            throws IOException {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/login?" + parametro);
    }
}
