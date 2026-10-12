package com.veterinaria.auth.controller;

import com.veterinaria.auth.dto.request.LoginRequest;
import com.veterinaria.auth.dto.request.RegisterRequest;
import com.veterinaria.auth.dto.response.AuthResponse;
import com.veterinaria.auth.dto.response.UsuarioResponse;
import com.veterinaria.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints PUBLICOS de autenticacion (/api/v1/auth).
 *
 * El controller NO tiene logica de negocio: solo recibe, valida el DTO (@Valid),
 * delega en el servicio y devuelve la respuesta HTTP.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Registro publico. Siempre crea un CLIENTE. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody RegisterRequest request) {
        return authService.registrar(request);
    }

    /** Login. Devuelve el token JWT. */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
