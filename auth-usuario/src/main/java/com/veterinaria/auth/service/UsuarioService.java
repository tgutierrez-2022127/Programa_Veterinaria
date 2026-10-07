package com.veterinaria.auth.service;

import com.veterinaria.auth.dto.request.LoginRequest;
import com.veterinaria.auth.dto.request.RegisterRequest;
import com.veterinaria.auth.dto.response.AuthResponse;
import com.veterinaria.auth.entity.Usuario;
import com.veterinaria.auth.enums.Rol;
import com.veterinaria.auth.exception.EmailDuplicadoException;
import com.veterinaria.auth.repository.UsuarioRepository;
import com.veterinaria.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio para autenticacion y registro de usuarios.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse registrar(RegisterRequest req) {
        if (usuarioRepository.existsByEmail(req.getEmail())) {
            throw new EmailDuplicadoException("El email ya esta registrado: " + req.getEmail());
        }

        Usuario usuario = Usuario.builder()
                .nombre(req.getNombre())
                .telefono(req.getTelefono())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .rol(Rol.CLIENTE)
                .build();

        usuarioRepository.save(usuario);

        UserDetails userDetails = construirUserDetails(usuario);
        String token = jwtService.generarToken(userDetails);

        return new AuthResponse(token, "Bearer", usuario.getEmail(), usuario.getRol().name());
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        Usuario usuario = usuarioRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        UserDetails userDetails = construirUserDetails(usuario);
        String token = jwtService.generarToken(userDetails);

        return new AuthResponse(token, "Bearer", usuario.getEmail(), usuario.getRol().name());
    }

    private UserDetails construirUserDetails(Usuario usuario) {
        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .authorities("ROLE_" + usuario.getRol().name())
                .build();
    }
}