package com.veterinaria.auth.controller;

import com.veterinaria.auth.dto.request.LoginRequest;
import com.veterinaria.auth.dto.request.RegisterRequest;
import com.veterinaria.auth.dto.response.AuthResponse;
import com.veterinaria.auth.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegisterRequest req) {
        AuthResponse response = usuarioService.registrar(req);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        AuthResponse response = usuarioService.login(req);
        return ResponseEntity.ok(response);
    }
}