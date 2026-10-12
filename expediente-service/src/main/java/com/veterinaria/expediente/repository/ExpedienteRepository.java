package com.veterinaria.expediente.repository;

import com.veterinaria.expediente.entity.ExpedienteClinico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de expedientes.
 *   findByMascotaId... -> historial clinico de una mascota (ordenado del mas reciente)
 *   findByCitaId       -> el expediente de una cita (la relacion es unica)
 */
@Repository
public interface ExpedienteRepository extends JpaRepository<ExpedienteClinico, Long> {

    List<ExpedienteClinico> findByMascotaIdOrderByFechaRegistroDesc(Long mascotaId);

    Optional<ExpedienteClinico> findByCitaId(Long citaId);

    boolean existsByCitaId(Long citaId);
}
