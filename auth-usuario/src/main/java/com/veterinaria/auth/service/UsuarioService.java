package com.veterinaria.auth.service;

import com.veterinaria.auth.dto.request.UsuarioCreateRequest;
import com.veterinaria.auth.dto.response.UsuarioResponse;
import com.veterinaria.auth.enums.Rol;

import java.util.List;

/**
 * Servicio de gestion de usuarios (operaciones administrativas y consultas internas).
 */
public interface UsuarioService {

    /** Crea un usuario con el rol indicado. Uso exclusivo del ADMIN. */
    UsuarioResponse crear(UsuarioCreateRequest request, Rol rolForzado);

    /** Lista todos los usuarios. */
    List<UsuarioResponse> listar();

    /** Lista solo veterinarios. */
    List<UsuarioResponse> listarVeterinarios();

    /** Obtiene un usuario por id o lanza RecursoNoEncontradoException. */
    UsuarioResponse obtener(Long id);
}
