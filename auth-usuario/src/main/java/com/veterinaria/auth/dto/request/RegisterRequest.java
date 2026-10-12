package com.veterinaria.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para el registro PUBLICO de usuarios.
 *
 * IMPORTANTE: este DTO NO tiene campo "rol". Es la tecnica principal para impedir
 * que un usuario publico se registre como ADMIN o VET: aunque el cliente envie
 * {"rol":"ADMIN"} en el JSON, Jackson simplemente lo ignora porque no existe el
 * campo (por defecto FAIL_ON_UNKNOWN_PROPERTIES esta desactivado en Spring).
 * El rol se asigna SIEMPRE en el servicio como CLIENTE.
 */
public record RegisterRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @Size(max = 20, message = "El telefono no puede superar 20 caracteres")
        String telefono,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        String email,

        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 6, max = 60, message = "La contrasena debe tener entre 6 y 60 caracteres")
        String password
) {
}
