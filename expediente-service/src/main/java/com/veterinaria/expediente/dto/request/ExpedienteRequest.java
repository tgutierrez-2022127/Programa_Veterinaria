package com.veterinaria.expediente.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO para registrar un expediente clinico a partir de una cita.
 * La fecha de registro la asigna el servidor automaticamente.
 */
public record ExpedienteRequest(

        @NotNull(message = "El id de la cita es obligatorio")
        Long citaId,

        @NotBlank(message = "El diagnostico es obligatorio")
        @Size(max = 1000, message = "El diagnostico no puede superar 1000 caracteres")
        String diagnostico,

        @NotBlank(message = "El tratamiento es obligatorio")
        @Size(max = 1000, message = "El tratamiento no puede superar 1000 caracteres")
        String tratamiento,

        @NotNull(message = "El peso es obligatorio")
        @Positive(message = "El peso debe ser un numero positivo")
        Double pesoKg
) {
}
