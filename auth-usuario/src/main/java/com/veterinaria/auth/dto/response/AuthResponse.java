package com.veterinaria.auth.dto.response;

/**
 * Respuesta del login: el token JWT y datos utiles para el cliente.
 */
public record AuthResponse(
        String token,
        String tipo,
        long expiraEnSegundos,
        UsuarioResponse usuario
) {
}
