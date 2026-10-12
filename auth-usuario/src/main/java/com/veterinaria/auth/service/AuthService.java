package com.veterinaria.auth.service;

import com.veterinaria.auth.dto.request.LoginRequest;
import com.veterinaria.auth.dto.request.RegisterRequest;
import com.veterinaria.auth.dto.response.AuthResponse;
import com.veterinaria.auth.dto.response.UsuarioResponse;

/**
 * Contrato del servicio de autenticacion.
 * Separar interfaz e implementacion permite cambiar la logica sin afectar a los controllers.
 */
public interface AuthService {

    /** Registro publico. El rol se fuerza SIEMPRE a CLIENTE. */
    UsuarioResponse registrar(RegisterRequest request);

    /** Login: valida credenciales y devuelve un JWT. */
    AuthResponse login(LoginRequest request);
}
