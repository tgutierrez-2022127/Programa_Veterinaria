package com.veterinaria.auth.dto.response;

import com.veterinaria.auth.entity.Usuario;
import com.veterinaria.auth.enums.Rol;

/**
 * DTO de salida. NUNCA exponemos la entidad Usuario directamente porque
 * contiene el hash de la contrasena (y ademas evita problemas de serializacion).
 */
public record UsuarioResponse(
        Long id,
        String nombre,
        String telefono,
        String email,
        Rol rol
) {
    /** Convierte Entity -> DTO. */
    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getTelefono(), u.getEmail(), u.getRol());
    }
}
