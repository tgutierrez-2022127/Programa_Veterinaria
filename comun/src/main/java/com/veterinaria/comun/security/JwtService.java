package com.veterinaria.comun.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Servicio encargado de GENERAR y VALIDAR los tokens JWT.
 *
 * El mismo secreto (jwt.secret) esta configurado en los 4 microservicios. Esto es
 * clave para la arquitectura stateless: cada servicio puede validar el token por si
 * mismo, sin preguntarle al servicio de autenticacion.
 *
 * Estructura del token:
 *   - sub      -> email del usuario
 *   - uid      -> id del usuario
 *   - rol      -> ADMIN | VET | CLIENTE
 *   - nombre   -> nombre del usuario
 *   - iat/exp  -> fecha de emision y expiracion
 */
@Component
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMs) {
        // Keys.hmacShaKeyFor exige una clave de al menos 256 bits (32 caracteres).
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Genera un token firmado con los datos del usuario.
     */
    public String generarToken(Long id, String email, String rol, String nombre) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("uid", id)
                .claim("rol", rol)
                .claim("nombre", nombre)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(key)
                .compact();
    }

    /**
     * Extrae todos los claims. Si el token es invalido o expirado lanza una excepcion
     * de jjwt (JwtException / ExpiredJwtException) que el filtro captura.
     */
    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public AuthenticatedUser convertirEnUsuario(String token) {
        Claims claims = extraerClaims(token);
        Long uid = claims.get("uid", Long.class);
        String rol = claims.get("rol", String.class);
        String nombre = claims.get("nombre", String.class);
        return new AuthenticatedUser(uid, claims.getSubject(), rol, nombre);
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
