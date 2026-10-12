package com.veterinaria.auth.service.impl;

import com.veterinaria.auth.dto.request.LoginRequest;
import com.veterinaria.auth.dto.request.RegisterRequest;
import com.veterinaria.auth.dto.response.AuthResponse;
import com.veterinaria.auth.dto.response.UsuarioResponse;
import com.veterinaria.auth.entity.Usuario;
import com.veterinaria.auth.enums.Rol;
import com.veterinaria.auth.repository.UsuarioRepository;
import com.veterinaria.auth.service.AuthService;
import com.veterinaria.comun.exception.CredencialesInvalidasException;
import com.veterinaria.comun.exception.EmailDuplicadoException;
import com.veterinaria.comun.exception.RecursoNoEncontradoException;
import com.veterinaria.comun.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementacion de autenticacion.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Registro publico.
     *
     * Punto clave de seguridad: NO se lee ningun rol del request. El rol se
     * asigna como CLIENTE siempre. Asi es imposible auto-asignarse ADMIN/VET.
     */
    @Override
    @Transactional
    public UsuarioResponse registrar(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailDuplicadoException("El email ya esta registrado: " + email);
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.nombre().trim())
                .telefono(request.telefono())
                .email(email)
                // Se guarda el HASH, no la contrasena en texto plano.
                .password(passwordEncoder.encode(request.password()))
                .rol(Rol.CLIENTE)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        return UsuarioResponse.from(guardado);
    }

    /**
     * Flujo de login:
     *  1. AuthenticationManager verifica email + contrasena (usa BCrypt internamente).
     *  2. Si es correcto, se busca el usuario para conocer id/rol/nombre.
     *  3. Se genera el JWT firmado.
     *  4. Se devuelve el token.
     */
    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password()));
        } catch (BadCredentialsException ex) {
            throw new CredencialesInvalidasException("Email o contrasena incorrectos");
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        String token = jwtService.generarToken(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getRol().name(),
                usuario.getNombre());

        return new AuthResponse(token, "Bearer", jwtService.getExpirationMs() / 1000,
                UsuarioResponse.from(usuario));
    }
}
