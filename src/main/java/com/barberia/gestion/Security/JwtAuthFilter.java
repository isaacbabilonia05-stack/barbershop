package com.barberia.gestion.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Si la peticion trae "Authorization: Bearer <token>", valida el token y autentica al usuario
 * (cargandolo de MongoDB). Si no trae token o es invalido, no hace nada: el resto de la cadena
 * sigue como antes (sesion + formulario de login).
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String PREFIJO = "Bearer ";
    /** Cookie donde el login con Google deja el JWT. */
    public static final String COOKIE_JWT = "JWT_TOKEN";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = extraerToken(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            jwtService.validarYObtenerEmail(token).ifPresent(email -> {
                try {
                    UserDetails usuario = userDetailsService.loadUserByUsername(email);
                    if (usuario.isEnabled()) {
                        var auth = new UsernamePasswordAuthenticationToken(
                                usuario, null, usuario.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                } catch (UsernameNotFoundException ignorado) {
                    // usuario borrado despues de emitir el token: queda sin autenticar
                }
            });
        }
        chain.doFilter(request, response);
    }

    /** Token de la cabecera "Authorization: Bearer ..." o, si no hay, de la cookie JWT_TOKEN. */
    private String extraerToken(HttpServletRequest request) {
        String cabecera = request.getHeader("Authorization");
        if (cabecera != null && cabecera.startsWith(PREFIJO)) {
            return cabecera.substring(PREFIJO.length()).trim();
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (COOKIE_JWT.equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}
