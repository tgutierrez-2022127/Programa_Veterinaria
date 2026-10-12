package com.veterinaria.mascota.client;

/**
 * Vista minima de un usuario que devuelve auth-usuario por su endpoint interno.
 */
public record UsuarioClienteDto(Long id, String nombre, String email, String rol) {
}
