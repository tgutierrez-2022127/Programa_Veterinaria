package com.veterinaria.mascota.controller;

import com.veterinaria.mascota.dto.response.MascotaResponse;
import com.veterinaria.mascota.service.MascotaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint INTERNO para otros microservicios (cita-service).
 * Devuelve la mascota incluyendo su clienteId para poder validar propiedad.
 * Protegido por X-Internal-Key (ver InternalApiFilter en comun).
 */
@RestController
@RequestMapping("/api/v1/internal/mascotas")
public class InternalMascotaController {

    private final MascotaService mascotaService;

    public InternalMascotaController(MascotaService mascotaService) {
        this.mascotaService = mascotaService;
    }

    @GetMapping("/{id}")
    public MascotaResponse obtener(@PathVariable Long id) {
        return mascotaService.obtener(id);
    }
}
