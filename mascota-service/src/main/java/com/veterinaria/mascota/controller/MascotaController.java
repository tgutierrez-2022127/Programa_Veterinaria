package com.veterinaria.mascota.controller;

import com.veterinaria.mascota.dto.request.MascotaRequest;
import com.veterinaria.mascota.dto.response.MascotaResponse;
import com.veterinaria.mascota.service.MascotaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mascotas")
@RequiredArgsConstructor
public class MascotaController {

    private final MascotaService mascotaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<MascotaResponse> crear(
            @Valid @RequestBody MascotaRequest req,
            Authentication authentication
    ) {
        String email = authentication.getName();
        Long clienteId = obtenerClienteIdDesdeEmail(email);
        MascotaResponse response = mascotaService.crear(req, clienteId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/mis-mascotas")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<MascotaResponse>> misMascotas(Authentication authentication) {
        String email = authentication.getName();
        Long clienteId = obtenerClienteIdDesdeEmail(email);
        return ResponseEntity.ok(mascotaService.listarPorCliente(clienteId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VET', 'ADMIN')")
    public ResponseEntity<MascotaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(mascotaService.obtenerPorId(id));
    }

    private Long obtenerClienteIdDesdeEmail(String email) {
        return (long) Math.abs(email.hashCode() % 1000) + 1;
    }
}