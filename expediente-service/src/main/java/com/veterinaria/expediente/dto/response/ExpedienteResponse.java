package com.veterinaria.expediente.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.veterinaria.expediente.entity.ExpedienteClinico;

import java.time.LocalDateTime;

/**
 * DTO de salida del expediente clinico.
 */
public record ExpedienteResponse(
        Long id,
        Long citaId,
        Long mascotaId,
        String diagnostico,
        String tratamiento,
        Double pesoKg,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime fechaRegistro
) {
    public static ExpedienteResponse from(ExpedienteClinico e) {
        return new ExpedienteResponse(e.getId(), e.getCitaId(), e.getMascotaId(),
                e.getDiagnostico(), e.getTratamiento(), e.getPesoKg(), e.getFechaRegistro());
    }
}
