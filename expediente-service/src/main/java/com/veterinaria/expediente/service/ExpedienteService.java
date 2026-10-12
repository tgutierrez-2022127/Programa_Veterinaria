package com.veterinaria.expediente.service;

import com.veterinaria.expediente.dto.request.ExpedienteRequest;
import com.veterinaria.expediente.dto.response.ExpedienteResponse;

import java.util.List;

/**
 * Contrato del servicio de expedientes clinicos.
 */
public interface ExpedienteService {

    /** Registra un expediente y marca la cita como COMPLETADA. */
    ExpedienteResponse registrar(ExpedienteRequest request);

    /** Historial de expedientes de una mascota (con control de acceso para CLIENTE). */
    List<ExpedienteResponse> historialPorMascota(Long mascotaId, Long usuarioId, String rol);

    /** Obtiene un expediente por id. */
    ExpedienteResponse obtener(Long id);
}
