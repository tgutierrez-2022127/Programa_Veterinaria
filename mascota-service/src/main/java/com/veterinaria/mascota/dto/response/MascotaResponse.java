package com.veterinaria.mascota.dto.response;

import com.veterinaria.mascota.entity.Mascota;
import com.veterinaria.mascota.enums.Especie;

/**
 * DTO de salida de una mascota. Expone el clienteId para que otros servicios
 * (cita-service) puedan verificar la propiedad de la mascota.
 */
public record MascotaResponse(
        Long id,
        String nombre,
        Especie especie,
        String raza,
        Integer edad,
        Long clienteId
) {
    public static MascotaResponse from(Mascota m) {
        return new MascotaResponse(m.getId(), m.getNombre(), m.getEspecie(),
                m.getRaza(), m.getEdad(), m.getClienteId());
    }
}
