package com.veterinaria.mascota.service;

import com.veterinaria.mascota.dto.request.MascotaRequest;
import com.veterinaria.mascota.dto.response.MascotaResponse;

import java.util.List;

/**
 * Contrato del servicio de mascotas.
 */
public interface MascotaService {

    /** Registra una mascota. El dueño se deduce del token (CLIENTE) o del body (ADMIN). */
    MascotaResponse registrar(MascotaRequest request, Long usuarioId, String rol);

    /** Lista las mascotas del cliente autenticado. */
    List<MascotaResponse> misMascotas(Long clienteId);

    /** Obtiene una mascota por id (uso VET/ADMIN e interno). */
    MascotaResponse obtener(Long id);

    /** Lista todas las mascotas (ADMIN). */
    List<MascotaResponse> listarTodas();
}
