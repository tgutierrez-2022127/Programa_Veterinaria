package com.veterinaria.cita.client;

/**
 * Vista minima de un usuario que devuelve auth-usuario (endpoint interno).
 */
public record UsuarioClienteDto(Long id, String nombre, String email, String rol) {
}
