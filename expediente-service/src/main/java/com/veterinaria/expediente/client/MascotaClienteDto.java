package com.veterinaria.expediente.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Vista minima de una mascota devuelta por mascota-service (endpoint interno).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MascotaClienteDto(Long id, String nombre, String especie, String raza,
                                Integer edad, Long clienteId) {
}
