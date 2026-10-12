package com.veterinaria.auth.service.impl;

import com.veterinaria.auth.dto.request.UsuarioCreateRequest;
import com.veterinaria.auth.dto.response.UsuarioResponse;
import com.veterinaria.auth.entity.Usuario;
import com.veterinaria.auth.enums.Rol;
import com.veterinaria.auth.repository.UsuarioRepository;
import com.veterinaria.auth.service.UsuarioService;
import com.veterinaria.comun.exception.EmailDuplicadoException;
import com.veterinaria.comun.exception.RecursoNoEncontradoException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementacion de la gestion de usuarios.
 */
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UsuarioResponse crear(UsuarioCreateRequest request, Rol rolForzado) {
        String email = request.email().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailDuplicadoException("El email ya esta registrado: " + email);
        }

        // Si rolForzado != null se ignora el rol del request (ej: endpoint /veterinarios -> VET).
        Rol rol = (rolForzado != null) ? rolForzado : request.rol();

        Usuario usuario = Usuario.builder()
                .nombre(request.nombre().trim())
                .telefono(request.telefono())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .rol(rol)
                .build();

        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarVeterinarios() {
        return usuarioRepository.findByRol(Rol.VET).stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id " + id));
        return UsuarioResponse.from(usuario);
    }
}
