package com.veterinaria.cita.service.impl;

import com.veterinaria.cita.client.AuthClient;
import com.veterinaria.cita.client.MascotaClienteDto;
import com.veterinaria.cita.client.MascotaClient;
import com.veterinaria.cita.client.UsuarioClienteDto;
import com.veterinaria.cita.dto.request.CitaRequest;
import com.veterinaria.cita.dto.response.CitaResponse;
import com.veterinaria.cita.entity.CitaMedica;
import com.veterinaria.cita.enums.EstadoCita;
import com.veterinaria.cita.repository.CitaRepository;
import com.veterinaria.cita.service.CitaService;
import com.veterinaria.comun.exception.AccesoDenegadoException;
import com.veterinaria.comun.exception.CancelacionNoPermitidaException;
import com.veterinaria.comun.exception.HorarioOcupadoException;
import com.veterinaria.comun.exception.LimiteCitasException;
import com.veterinaria.comun.exception.RecursoNoEncontradoException;
import com.veterinaria.comun.exception.ReglaNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementacion de las reglas de negocio de citas.
 */
@Service
public class CitaServiceImpl implements CitaService {

    /** Duracion FIJA de cada cita, en minutos (regla del sistema). */
    private static final int DURACION_MINUTOS = 30;
    /** Anticipacion minima para poder cancelar (regla 3). */
    private static final long HORAS_MINIMAS_CANCELACION = 2;
    /** Maximo de citas PENDIENTES por cliente en un mismo dia (regla 2). */
    private static final long MAX_CITAS_PENDIENTES_DIA = 2;

    private final CitaRepository citaRepository;
    private final MascotaClient mascotaClient;
    private final AuthClient authClient;

    public CitaServiceImpl(CitaRepository citaRepository,
                           MascotaClient mascotaClient,
                           AuthClient authClient) {
        this.citaRepository = citaRepository;
        this.mascotaClient = mascotaClient;
        this.authClient = authClient;
    }

    @Override
    @Transactional
    public CitaResponse crear(CitaRequest request, Long usuarioId, String rol) {
        LocalDateTime inicio = request.fechaHora();

        // ---- Validacion 1: la mascota existe (consulta a mascota-service) ----
        MascotaClienteDto mascota = mascotaClient.obtenerMascota(request.mascotaId());
        Long clienteId = mascota.clienteId();

        // ---- Validacion 2: si quien crea es CLIENTE, la mascota debe ser suya ----
        if ("CLIENTE".equals(rol) && !clienteId.equals(usuarioId)) {
            throw new AccesoDenegadoException(
                    "La mascota no pertenece al cliente autenticado; no puede agendarle una cita");
        }

        // ---- Validacion 3: el veterinario existe y tiene rol VET ----
        UsuarioClienteDto veterinario = authClient.obtenerUsuario(request.veterinarioId());
        if (veterinario == null || !"VET".equals(veterinario.rol())) {
            throw new ReglaNegocioException(
                    "El usuario con id " + request.veterinarioId() + " no es un veterinario");
        }

        // ---- Validacion 4: la fecha debe ser futura ----
        if (!inicio.isAfter(LocalDateTime.now())) {
            throw new ReglaNegocioException("La fecha y hora de la cita deben ser futuras");
        }

        // ---- REGLA 1: disponibilidad del veterinario (sin cruce de horarios) ----
        // Dos citas de 30 min se cruzan si el inicio de una cae dentro de +-30 min de la otra.
        long cruces = citaRepository.contarCitasEnVentana(
                request.veterinarioId(),
                inicio.minusMinutes(DURACION_MINUTOS),
                inicio.plusMinutes(DURACION_MINUTOS));

        if (cruces > 0) {
            throw new HorarioOcupadoException(
                    "El veterinario ya tiene una cita en ese horario (las citas duran "
                            + DURACION_MINUTOS + " minutos)");
        }

        // ---- REGLA 2: maximo 2 citas PENDIENTES del cliente en el mismo dia ----
        LocalDateTime diaInicio = inicio.toLocalDate().atStartOfDay();
        LocalDateTime diaFin = inicio.toLocalDate().atTime(23, 59, 59);

        long pendientes = citaRepository.countByClienteIdAndEstadoAndFechaHoraBetween(
                clienteId, EstadoCita.PENDIENTE, diaInicio, diaFin);

        if (pendientes >= MAX_CITAS_PENDIENTES_DIA) {
            throw new LimiteCitasException(
                    "El cliente ya tiene " + MAX_CITAS_PENDIENTES_DIA
                            + " citas PENDIENTES para el dia " + inicio.toLocalDate()
                            + "; no puede programar mas");
        }

        // ---- Creacion de la cita ----
        CitaMedica cita = CitaMedica.builder()
                .mascotaId(request.mascotaId())
                .veterinarioId(request.veterinarioId())
                .clienteId(clienteId)
                .fechaHora(inicio)
                .motivo(request.motivo().trim())
                .estado(EstadoCita.PENDIENTE)
                .build();

        return CitaResponse.from(citaRepository.save(cita));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponse> agenda(LocalDate fecha, Long veterinarioId, Long usuarioId, String rol) {
        LocalDate dia = (fecha != null) ? fecha : LocalDate.now();
        LocalDateTime inicio = dia.atStartOfDay();
        LocalDateTime fin = dia.atTime(23, 59, 59);

        List<CitaMedica> citas;

        if ("VET".equals(rol)) {
            // Un veterinario SIEMPRE ve su propia agenda (no puede espiar la de otro).
            citas = citaRepository.findByVeterinarioIdAndFechaHoraBetweenOrderByFechaHoraAsc(
                    usuarioId, inicio, fin);
        } else if (veterinarioId != null) {
            // ADMIN filtrando por un veterinario concreto.
            citas = citaRepository.findByVeterinarioIdAndFechaHoraBetweenOrderByFechaHoraAsc(
                    veterinarioId, inicio, fin);
        } else {
            // ADMIN viendo todas las citas del dia.
            citas = citaRepository.findByFechaHoraBetweenOrderByFechaHoraAsc(inicio, fin);
        }

        return citas.stream().map(CitaResponse::from).toList();
    }

    @Override
    @Transactional
    public CitaResponse cancelar(Long citaId, Long usuarioId, String rol) {
        CitaMedica cita = buscar(citaId);

        // Un CLIENTE solo puede cancelar sus propias citas.
        if ("CLIENTE".equals(rol) && !cita.getClienteId().equals(usuarioId)) {
            throw new AccesoDenegadoException("La cita no pertenece al cliente autenticado");
        }

        if (cita.getEstado() != EstadoCita.PENDIENTE) {
            throw new ReglaNegocioException(
                    "Solo se pueden cancelar citas en estado PENDIENTE (estado actual: "
                            + cita.getEstado() + ")");
        }

        // ---- REGLA 3: solo se cancela si faltan MAS de 2 horas ----
        Duration tiempoRestante = Duration.between(LocalDateTime.now(), cita.getFechaHora());
        if (tiempoRestante.toMinutes() <= HORAS_MINIMAS_CANCELACION * 60) {
            throw new CancelacionNoPermitidaException(
                    "No se puede cancelar: faltan 2 horas o menos para la cita");
        }

        cita.setEstado(EstadoCita.CANCELADA);
        return CitaResponse.from(citaRepository.save(cita));
    }

    @Override
    @Transactional(readOnly = true)
    public CitaResponse obtener(Long citaId) {
        return CitaResponse.from(buscar(citaId));
    }

    @Override
    @Transactional
    public CitaResponse completar(Long citaId) {
        CitaMedica cita = buscar(citaId);
        cita.setEstado(EstadoCita.COMPLETADA);
        return CitaResponse.from(citaRepository.save(cita));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponse> porMascota(Long mascotaId) {
        return citaRepository.findByMascotaIdOrderByFechaHoraDesc(mascotaId).stream()
                .map(CitaResponse::from)
                .toList();
    }

    private CitaMedica buscar(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada con id " + id));
    }
}
