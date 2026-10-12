package com.veterinaria.cita.controller;

import com.veterinaria.cita.dto.request.CitaRequest;
import com.veterinaria.cita.dto.response.CitaResponse;
import com.veterinaria.cita.service.CitaService;
import com.veterinaria.comun.security.AuthenticatedUser;
import com.veterinaria.comun.security.PrincipalUtils;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Endpoints de citas (/api/v1/citas).
 */
@RestController
@RequestMapping("/api/v1/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    /** Crea una cita (CLIENTE o ADMIN). Aplica reglas 1, 2 y validaciones. */
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public CitaResponse crear(@Valid @RequestBody CitaRequest request) {
        AuthenticatedUser usuario = PrincipalUtils.usuarioActual();
        return citaService.crear(request, usuario.id(), usuario.rol());
    }

    /**
     * Agenda por fecha y, opcionalmente, por veterinario.
     * Ejemplos:
     *   GET /api/v1/citas/agenda
     *   GET /api/v1/citas/agenda?fecha=2026-03-10
     *   GET /api/v1/citas/agenda?fecha=2026-03-10&veterinarioId=2
     */
    @GetMapping("/agenda")
    @PreAuthorize("hasAnyRole('VET','ADMIN')")
    public List<CitaResponse> agenda(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Long veterinarioId) {
        AuthenticatedUser usuario = PrincipalUtils.usuarioActual();
        return citaService.agenda(fecha, veterinarioId, usuario.id(), usuario.rol());
    }

    /** Cancela una cita validando la regla de las 2 horas. */
    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN')")
    public CitaResponse cancelar(@PathVariable Long id) {
        AuthenticatedUser usuario = PrincipalUtils.usuarioActual();
        return citaService.cancelar(id, usuario.id(), usuario.rol());
    }

    /** Consulta una cita por id (VET, ADMIN). */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VET','ADMIN')")
    public CitaResponse obtener(@PathVariable Long id) {
        return citaService.obtener(id);
    }
}
