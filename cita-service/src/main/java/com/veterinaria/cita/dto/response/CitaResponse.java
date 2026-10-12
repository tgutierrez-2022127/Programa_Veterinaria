package com.veterinaria.cita.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.veterinaria.cita.entity.CitaMedica;
import com.veterinaria.cita.enums.EstadoCita;

import java.time.LocalDateTime;

/**
 * DTO de salida de una cita. Incluye "fechaFin" (inicio + 30 min) para que el
 * cliente vea claramente el rango.
 */
public record CitaResponse(
        Long id,
        Long mascotaId,
        Long veterinarioId,
        Long clienteId,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime fechaHora,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime fechaFin,
        String motivo,
        EstadoCita estado
) {
    public static CitaResponse from(CitaMedica c) {
        return new CitaResponse(
                c.getId(),
                c.getMascotaId(),
                c.getVeterinarioId(),
                c.getClienteId(),
                c.getFechaHora(),
                c.getFechaHora().plusMinutes(30),
                c.getMotivo(),
                c.getEstado());
    }
}
