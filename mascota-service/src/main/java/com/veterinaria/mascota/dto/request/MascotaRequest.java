package com.veterinaria.mascota.dto.request;

import com.veterinaria.mascota.enums.Especie;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO para registrar una mascota.
 *
 * "clienteId" es OPCIONAL:
 *  - Si quien crea es un CLIENTE, se IGNORA y se usa el id del token JWT.
 *  - Si quien crea es un ADMIN, debe indicarse para saber a quien pertenece.
 */
public record MascotaRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @NotNull(message = "La especie es obligatoria")
        Especie especie,

        @Size(max = 100, message = "La raza no puede superar 100 caracteres")
        String raza,

        @Positive(message = "La edad debe ser un numero positivo")
        Integer edad,

        Long clienteId
) {
}
