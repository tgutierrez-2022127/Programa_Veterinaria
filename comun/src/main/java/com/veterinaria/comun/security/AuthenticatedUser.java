package com.veterinaria.comun.security;

/**
 * Representa al usuario autenticado que viaja dentro del JWT.
 *
 * Cuando el token es valido, este objeto se guarda como "principal" dentro del
 * SecurityContext de Spring Security. Asi, cualquier capa (Controller/Service)
 * puede saber QUIEN esta haciendo la peticion sin volver a consultar la base de datos.
 *
 * @param id     identificador del usuario (claim "uid")
 * @param email  correo del usuario (claim "sub")
 * @param rol    rol del usuario: ADMIN, VET o CLIENTE (claim "rol")
 * @param nombre nombre del usuario (claim "nombre")
 */
public record AuthenticatedUser(Long id, String email, String rol, String nombre) {
}
