package com.veterinaria.auth.dto.request;

import com.veterinaria.auth.enums.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO usado SOLO por el ADMIN para crear usuarios de cualquier rol
 * (por ejemplo, registrar veterinarios).
 */
public record UsuarioCreateRequest(

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
        String password,

        @NotNull(message = "El rol es obligatorio")
        Rol rol
) {
}
