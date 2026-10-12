package com.veterinaria.cita.service;

import com.veterinaria.cita.dto.request.CitaRequest;
import com.veterinaria.cita.dto.response.CitaResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrato del servicio de citas.
 */
public interface CitaService {

    /** Crea una cita aplicando todas las reglas de negocio. */
    CitaResponse crear(CitaRequest request, Long usuarioId, String rol);

    /** Agenda de citas por fecha y (opcionalmente) veterinario. */
    List<CitaResponse> agenda(LocalDate fecha, Long veterinarioId, Long usuarioId, String rol);

    /** Cancela una cita validando la regla de las 2 horas. */
    CitaResponse cancelar(Long citaId, Long usuarioId, String rol);

    /** Obtiene una cita por id. */
    CitaResponse obtener(Long citaId);

    /** Marca una cita como COMPLETADA (uso interno desde expediente-service). */
    CitaResponse completar(Long citaId);

    /** Citas de una mascota (historial). */
    List<CitaResponse> porMascota(Long mascotaId);
}
