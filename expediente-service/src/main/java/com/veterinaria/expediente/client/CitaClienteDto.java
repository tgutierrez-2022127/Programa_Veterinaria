package com.veterinaria.expediente.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * Vista minima de una cita devuelta por cita-service (endpoint interno).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CitaClienteDto(Long id, Long mascotaId, Long veterinarioId, Long clienteId,
                             LocalDateTime fechaHora, String motivo, String estado) {
}
