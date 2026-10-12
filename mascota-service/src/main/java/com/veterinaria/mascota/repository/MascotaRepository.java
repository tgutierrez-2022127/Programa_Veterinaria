package com.veterinaria.mascota.repository;

import com.veterinaria.mascota.entity.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de mascotas.
 *
 * Consultas derivadas:
 *   findByClienteId            -> todas las mascotas de un cliente
 *   findByIdAndClienteId       -> una mascota solo si pertenece al cliente (SEGURIDAD)
 *   existsByIdAndClienteId     -> verificar pertenencia sin traer datos
 */
@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    List<Mascota> findByClienteId(Long clienteId);

    java.util.Optional<Mascota> findByIdAndClienteId(Long id, Long clienteId);

    boolean existsByIdAndClienteId(Long id, Long clienteId);
}
