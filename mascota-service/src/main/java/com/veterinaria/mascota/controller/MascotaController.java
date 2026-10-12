package com.veterinaria.mascota.controller;

import com.veterinaria.comun.security.AuthenticatedUser;
import com.veterinaria.comun.security.PrincipalUtils;
import com.veterinaria.mascota.dto.request.MascotaRequest;
import com.veterinaria.mascota.dto.response.MascotaResponse;
import com.veterinaria.mascota.service.MascotaService;
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
 * Endpoints de mascotas (/api/v1/mascotas).
 */
@RestController
@RequestMapping("/api/v1/mascotas")
public class MascotaController {

    private final MascotaService mascotaService;

    public MascotaController(MascotaService mascotaService) {
        this.mascotaService = mascotaService;
    }

    /**
     * Devuelve SOLO las mascotas del cliente autenticado.
     *
     * SEGURIDAD: el id del cliente se obtiene del token JWT (PrincipalUtils), NO de un
     * parametro que el cliente pueda manipular. Asi no puede ver mascotas de otros.
     */
    @GetMapping("/mis-mascotas")
    @PreAuthorize("hasRole('CLIENTE')")
    public List<MascotaResponse> misMascotas() {
        AuthenticatedUser usuario = PrincipalUtils.usuarioActual();
        return mascotaService.misMascotas(usuario.id());
    }

    /** Registra una mascota (CLIENTE registra las suyas; ADMIN puede registrar para un cliente). */
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public MascotaResponse registrar(@Valid @RequestBody MascotaRequest request) {
        AuthenticatedUser usuario = PrincipalUtils.usuarioActual();
        return mascotaService.registrar(request, usuario.id(), usuario.rol());
    }

    /** Consulta una mascota por id (solo VET y ADMIN). */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VET','ADMIN')")
    public MascotaResponse obtener(@PathVariable Long id) {
        return mascotaService.obtener(id);
    }

    /** Lista todas las mascotas (solo ADMIN). */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<MascotaResponse> listar() {
        return mascotaService.listarTodas();
    }
}
