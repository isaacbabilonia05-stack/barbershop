package com.barberia.gestion.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
public class SecurityConfig {

    /** Encriptador de contrasenas (BCrypt). */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Necesario para que el control de sesiones concurrentes se entere cuando una sesion expira o se cierra. */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    /**
     * La autenticacion la hace Spring Security: toma el UserDetailsService
     * (CustomUserDetailsService, que lee MongoDB) y el PasswordEncoder (BCrypt) definidos como beans.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           LoginSuccessHandler successHandler,
                                           LoginFailureHandler failureHandler,
                                           JwtAuthFilter jwtAuthFilter,
                                           OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler) throws Exception {
        http
            // CSRF ACTIVO: todos los formularios usan th:action (Thymeleaf agrega el token solo)
            // JWT: solo se omite en POST /api/auth/login y en peticiones que traen "Authorization: Bearer ..."
            // (un navegador no agrega esa cabecera solo, asi que no hay riesgo de CSRF en ellas)
            .csrf(csrf -> csrf.ignoringRequestMatchers(
                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/auth/login"),
                request -> {
                    String h = request.getHeader("Authorization");
                    return h != null && h.startsWith(JwtAuthFilter.PREFIJO);
                }))
            .authorizeHttpRequests(auth -> auth
                // Recursos estaticos de la raiz de /static
                .requestMatchers("/style.css", "/*.css", "/*.js", "/*.jpg", "/*.jpeg",
                                 "/*.png", "/*.gif", "/*.ico", "/favicon.ico").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                // JWT: obtener el token (publico). /api/auth/me exige token o sesion (regla anyRequest)
                .requestMatchers("/api/auth/login").permitAll()
                // Paginas publicas
                .requestMatchers("/login", "/registro", "/error", "/acceso-denegado").permitAll()
                // OAuth2 (Google): inicio del flujo y callback de Google
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                // Solo ADMIN
                .requestMatchers("/clientes/**", "/admin/**").hasRole("ADMIN")
                // Solo BARBERO
                .requestMatchers("/cita/**", "/barbero/**").hasRole("BARBERO")
                // Solo CLIENTE
                .requestMatchers("/menu", "/perfil", "/agendar", "/miscitas",
                                 "/cancelarCita/**", "/calificarCita/**").hasRole("CLIENTE")
                // Endpoints AJAX de agendar.html (los usa el cliente que esta agendando)
                .requestMatchers("/disponibilidad", "/horasOcupadas", "/horasOcupadasConEstado",
                                 "/diasLibres").hasRole("CLIENTE")
                // Cualquier otra ruta: solo con sesion iniciada
                .anyRequest().authenticated()
            )
            // Formulario de login propio (templates/login.html)
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .permitAll()
            )
            // Login con Google: al terminar bien, OAuth2AuthenticationSuccessHandler busca/crea el usuario en Mongo,
            // genera el JWT y deja la sesion igual que el login por formulario. El formulario NO se desactiva.
            .oauth2Login(oauth -> oauth
                .loginPage("/login")
                .successHandler(oAuth2SuccessHandler)
                .failureUrl("/login?oauth_error")
            )
            .logout(logout -> logout
                .logoutUrl("/logout")                       // POST /logout (con token CSRF)
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID", "JWT_TOKEN")
                .permitAll()
            )
            // Administracion de sesiones
            .sessionManagement(session -> session
                .invalidSessionUrl("/login?expirado")       // sesion vencida o inexistente
                .maximumSessions(1)                         // una sesion activa por usuario
                    .expiredUrl("/login?expirado")
            )
            // JWT: el filtro corre antes del login por formulario; si no hay token no cambia nada
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            // JWT: las rutas /api/** sin credenciales responden 401 (en vez de redirigir a /login)
            .exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                PathPatternRequestMatcher.withDefaults().matcher("/api/**")))
            // Usuario logueado sin permiso para la ruta -> pagina 403 amigable
            .exceptionHandling(ex -> ex.accessDeniedPage("/acceso-denegado"));

        return http.build();
    }
}
