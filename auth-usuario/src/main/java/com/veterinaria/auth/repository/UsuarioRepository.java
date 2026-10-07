package com.veterinaria.auth.repository;

import com.veterinaria.auth.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio de Usuario. Hereda el CRUD de JpaRepository.
 * Los métodos findByEmail y existsByEmail son generados automáticamente por Spring Data.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}