package com.veterinaria.auth.controller;

import com.veterinaria.auth.dto.response.UsuarioResponse;
import com.veterinaria.auth.service.UsuarioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints INTERNOS para comunicacion entre microservicios.
 *
 * Ruta /api/v1/internal/** -> no usa JWT de usuario, se protege con la clave
 * compartida "X-Internal-Key" (ver InternalApiFilter en el modulo comun).
 *
 * Los usa, por ejemplo, cita-service para validar que un veterinario existe y
 * que un cliente existe.
 */
@RestController
@RequestMapping("/api/v1/internal/usuarios")
public class InternalUsuarioController {

    private final UsuarioService usuarioService;

    public InternalUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }
}
