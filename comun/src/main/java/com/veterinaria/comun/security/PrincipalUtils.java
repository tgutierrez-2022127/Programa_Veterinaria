package com.veterinaria.comun.security;

import com.veterinaria.comun.exception.AccesoDenegadoException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utilidad para obtener el usuario autenticado desde el SecurityContext.
 *
 * ESTA ES LA CLAVE DE LA SEGURIDAD DE MASCOTAS Y EXPEDIENTES:
 * el id del usuario NO se recibe desde el body/URL (seria manipulable);
 * se obtiene del token JWT ya validado.
 */
public final class PrincipalUtils {

    private PrincipalUtils() {
    }

    public static AuthenticatedUser usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthenticatedUser usuario)) {
            throw new AccesoDenegadoException("No hay un usuario autenticado en el contexto.");
        }
        return usuario;
    }
}
