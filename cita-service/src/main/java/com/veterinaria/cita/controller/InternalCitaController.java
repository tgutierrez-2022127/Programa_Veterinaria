package com.veterinaria.cita.controller;

import com.veterinaria.cita.dto.response.CitaResponse;
import com.veterinaria.cita.service.CitaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints INTERNOS usados por expediente-service.
 *  - Consultar una cita (para conocer mascota y estado).
 *  - Marcar la cita como COMPLETADA cuando se registra su expediente.
 * Protegidos por X-Internal-Key.
 */
@RestController
@RequestMapping("/api/v1/internal/citas")
public class InternalCitaController {

    private final CitaService citaService;

    public InternalCitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @GetMapping("/{id}")
    public CitaResponse obtener(@PathVariable Long id) {
        return citaService.obtener(id);
    }

    @PatchMapping("/{id}/completar")
    public CitaResponse completar(@PathVariable Long id) {
        return citaService.completar(id);
    }
}
