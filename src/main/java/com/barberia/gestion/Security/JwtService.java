package com.barberia.gestion.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

/** Genera y valida los JSON Web Tokens (firmados con HS256). */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(@Value("${app.jwt.secret}") String secreto,
                      @Value("${app.jwt.expiracion-minutos:15}") long expiracionMinutos) {
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret debe tener al menos 32 caracteres");
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.expiracionMs = expiracionMinutos * 60_000L;
    }

    public long getExpiracionSegundos() {
        return expiracionMs / 1000;
    }

    /** Crea un token con el email como "subject" y el rol, nombre e id como claims. */
    public String generarToken(UsuarioPrincipal usuario) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("rol", usuario.getRol())
                .claim("nombre", usuario.getNombre())
                .claim("id", usuario.getId())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave)
                .compact();
    }

    /** Devuelve el email del token si la firma es valida y no ha expirado; si no, vacio. */
    public Optional<String> validarYObtenerEmail(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(clave).build()
                    .parseSignedClaims(token).getPayload();
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
