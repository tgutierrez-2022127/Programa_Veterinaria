package com.veterinaria.expediente.controller;

import com.veterinaria.comun.security.AuthenticatedUser;
import com.veterinaria.comun.security.PrincipalUtils;
import com.veterinaria.expediente.dto.request.ExpedienteRequest;
import com.veterinaria.expediente.dto.response.ExpedienteResponse;
import com.veterinaria.expediente.service.ExpedienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de expedientes clinicos (/api/v1/expedientes).
 */
@RestController
@RequestMapping("/api/v1/expedientes")
public class ExpedienteController {

    private final ExpedienteService expedienteService;

    public ExpedienteController(ExpedienteService expedienteService) {
        this.expedienteService = expedienteService;
    }

    /** Registra un expediente y marca la cita como COMPLETADA (VET o ADMIN). */
    @PostMapping
    @PreAuthorize("hasAnyRole('VET','ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ExpedienteResponse registrar(@Valid @RequestBody ExpedienteRequest request) {
        return expedienteService.registrar(request);
    }

    /**
     * Historial de expedientes de una mascota.
     * VET y ADMIN ven cualquiera; un CLIENTE solo si la mascota es suya
     * (la verificacion de propiedad se hace en el servicio).
     */
    @GetMapping("/mascota/{mascotaId}")
    @PreAuthorize("hasAnyRole('VET','CLIENTE','ADMIN')")
    public List<ExpedienteResponse> historial(@PathVariable Long mascotaId) {
        AuthenticatedUser usuario = PrincipalUtils.usuarioActual();
        return expedienteService.historialPorMascota(mascotaId, usuario.id(), usuario.rol());
    }

    /** Consulta un expediente por id (VET, ADMIN). */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VET','ADMIN')")
    public ExpedienteResponse obtener(@PathVariable Long id) {
        return expedienteService.obtener(id);
    }
}
