package com.veterinaria.cita.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * DTO para crear una cita.
 *
 * Validaciones de DATOS (@NotNull, @Future, @NotBlank): formato/completitud.
 * Las REGLAS DE NEGOCIO (disponibilidad, limite de citas) se validan en el servicio.
 */
public record CitaRequest(

        @NotNull(message = "El id de la mascota es obligatorio")
        Long mascotaId,

        @NotNull(message = "El id del veterinario es obligatorio")
        Long veterinarioId,

        @NotNull(message = "La fecha y hora son obligatorias")
        @Future(message = "La fecha y hora de la cita deben ser futuras")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime fechaHora,

        @NotBlank(message = "El motivo es obligatorio")
        @Size(max = 255, message = "El motivo no puede superar 255 caracteres")
        String motivo
) {
}
