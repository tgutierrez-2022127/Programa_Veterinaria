package com.veterinaria.expediente.service.impl;

import com.veterinaria.comun.exception.AccesoDenegadoException;
import com.veterinaria.comun.exception.RecursoNoEncontradoException;
import com.veterinaria.comun.exception.ReglaNegocioException;
import com.veterinaria.expediente.client.CitaClienteDto;
import com.veterinaria.expediente.client.CitaClient;
import com.veterinaria.expediente.client.MascotaClienteDto;
import com.veterinaria.expediente.client.MascotaClient;
import com.veterinaria.expediente.dto.request.ExpedienteRequest;
import com.veterinaria.expediente.dto.response.ExpedienteResponse;
import com.veterinaria.expediente.entity.ExpedienteClinico;
import com.veterinaria.expediente.repository.ExpedienteRepository;
import com.veterinaria.expediente.service.ExpedienteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Logica de negocio de expedientes clinicos.
 */
@Service
public class ExpedienteServiceImpl implements ExpedienteService {

    private final ExpedienteRepository expedienteRepository;
    private final CitaClient citaClient;
    private final MascotaClient mascotaClient;

    public ExpedienteServiceImpl(ExpedienteRepository expedienteRepository,
                                 CitaClient citaClient,
                                 MascotaClient mascotaClient) {
        this.expedienteRepository = expedienteRepository;
        this.citaClient = citaClient;
        this.mascotaClient = mascotaClient;
    }

    @Override
    @Transactional
    public ExpedienteResponse registrar(ExpedienteRequest request) {
        // 1. La cita debe existir (consulta a cita-service).
        CitaClienteDto cita = citaClient.obtenerCita(request.citaId());

        // 2. No puede haber dos expedientes para la misma cita (relacion unica).
        if (expedienteRepository.existsByCitaId(request.citaId())) {
            throw new ReglaNegocioException(
                    "La cita " + request.citaId() + " ya tiene un expediente registrado");
        }

        // 3. No se registran expedientes de citas canceladas.
        if ("CANCELADA".equals(cita.estado())) {
            throw new ReglaNegocioException("No se puede registrar un expediente de una cita cancelada");
        }

        // 4. Crear el expediente.
        ExpedienteClinico expediente = ExpedienteClinico.builder()
                .citaId(request.citaId())
                .mascotaId(cita.mascotaId())
                .diagnostico(request.diagnostico().trim())
                .tratamiento(request.tratamiento().trim())
                .pesoKg(request.pesoKg())
                .fechaRegistro(LocalDateTime.now())
                .build();

        ExpedienteClinico guardado = expedienteRepository.save(expediente);

        // 5. Cambiar la cita a COMPLETADA (regla del enunciado).
        citaClient.completarCita(request.citaId());

        return ExpedienteResponse.from(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpedienteResponse> historialPorMascota(Long mascotaId, Long usuarioId, String rol) {
        // SEGURIDAD: un CLIENTE solo puede ver el historial de SUS mascotas.
        if ("CLIENTE".equals(rol)) {
            MascotaClienteDto mascota = mascotaClient.obtenerMascota(mascotaId);
            if (!mascota.clienteId().equals(usuarioId)) {
                throw new AccesoDenegadoException(
                        "La mascota no pertenece al cliente autenticado");
            }
        }

        return expedienteRepository.findByMascotaIdOrderByFechaRegistroDesc(mascotaId).stream()
                .map(ExpedienteResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ExpedienteResponse obtener(Long id) {
        ExpedienteClinico expediente = expedienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Expediente no encontrado con id " + id));
        return ExpedienteResponse.from(expediente);
    }
}
