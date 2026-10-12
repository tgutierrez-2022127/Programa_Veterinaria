package com.veterinaria.cita.client;

/**
 * Vista minima de una mascota que devuelve mascota-service (endpoint interno).
 * Se incluye clienteId para verificar la propiedad desde cita-service.
 */
public record MascotaClienteDto(Long id, String nombre, String especie, String raza,
                                Integer edad, Long clienteId) {
}
