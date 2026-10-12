package com.veterinaria.auth.controller;

import com.veterinaria.auth.dto.request.UsuarioCreateRequest;
import com.veterinaria.auth.dto.response.UsuarioResponse;
import com.veterinaria.auth.enums.Rol;
import com.veterinaria.auth.service.UsuarioService;
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
 * Gestion de usuarios (/api/v1/usuarios). Uso administrativo.
 *
 * @PreAuthorize comprueba el rol ANTES de ejecutar el metodo. Si el rol no coincide,
 * Spring Security lanza AccessDeniedException -> 403 Forbidden (via GlobalExceptionHandler).
 */
@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** Crea un usuario con el rol que venga en el body (ADMIN). */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@Valid @RequestBody UsuarioCreateRequest request) {
        return usuarioService.crear(request, null);
    }

    /** Registra un veterinario. Fuerza el rol VET (ignora el del body). */
    @PostMapping("/veterinarios")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crearVeterinario(@Valid @RequestBody UsuarioCreateRequest request) {
        return usuarioService.crear(request, Rol.VET);
    }

    /** Lista todos los usuarios (ADMIN). */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    /** Lista solo veterinarios (ADMIN y VET, util para agendar). */
    @GetMapping("/veterinarios")
    @PreAuthorize("hasAnyRole('ADMIN','VET')")
    public List<UsuarioResponse> listarVeterinarios() {
        return usuarioService.listarVeterinarios();
    }

    /** Consulta un usuario por id (ADMIN). */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }
}
