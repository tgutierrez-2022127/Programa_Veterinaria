package com.veterinaria.auth.repository;

import com.veterinaria.auth.entity.Usuario;
import com.veterinaria.auth.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de usuarios.
 *
 * Aqui vemos "consultas derivadas": Spring Data JPA genera el SQL a partir del
 * NOMBRE del metodo. No hace falta escribir la consulta.
 *   findByEmail      -> WHERE email = ?
 *   existsByEmail    -> SELECT count(*) > 0 WHERE email = ?
 *   findByRol        -> WHERE rol = ?
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Usuario> findByRol(Rol rol);
}
