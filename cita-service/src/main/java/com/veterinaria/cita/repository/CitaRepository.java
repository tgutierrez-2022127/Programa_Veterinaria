package com.veterinaria.cita.repository;

import com.veterinaria.cita.entity.CitaMedica;
import com.veterinaria.cita.enums.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio de citas con consultas derivadas y JPQL.
 */
@Repository
public interface CitaRepository extends JpaRepository<CitaMedica, Long> {

    /**
     * REGLA 1 - Disponibilidad del veterinario.
     *
     * Como las citas duran 30 minutos, dos citas se cruzan si la hora de inicio de
     * una esta dentro de la "ventana" de +-30 minutos alrededor de la otra.
     *
     * Se excluyen las citas CANCELADAS porque ya no ocupan el horario.
     *
     * Ejemplo: nueva cita 10:15 -> ventana (09:45, 10:45).
     *   - cita 10:00 (esta en la ventana) -> HAY cruce.
     *   - cita 10:30 (esta en la ventana) -> HAY cruce.
     * Nueva cita 10:30 -> ventana (10:00, 11:00):
     *   - cita 10:00 (10:00 NO es > 10:00) -> NO hay cruce (son consecutivas, valido).
     */
    @Query("""
            SELECT COUNT(c) FROM CitaMedica c
            WHERE c.veterinarioId = :veterinarioId
              AND c.estado <> com.veterinaria.cita.enums.EstadoCita.CANCELADA
              AND c.fechaHora > :desde
              AND c.fechaHora < :hasta
            """)
    long contarCitasEnVentana(@Param("veterinarioId") Long veterinarioId,
                              @Param("desde") LocalDateTime desde,
                              @Param("hasta") LocalDateTime hasta);

    /**
     * REGLA 2 - Limite de 2 citas PENDIENTES del cliente en un mismo dia.
     * Consulta derivada: count ... where clienteId = ? and estado = ? and fechaHora between ?
     */
    long countByClienteIdAndEstadoAndFechaHoraBetween(Long clienteId,
                                                      EstadoCita estado,
                                                      LocalDateTime inicio,
                                                      LocalDateTime fin);

    /** Agenda: citas de un veterinario en un rango (normalmente un dia). */
    List<CitaMedica> findByVeterinarioIdAndFechaHoraBetweenOrderByFechaHoraAsc(
            Long veterinarioId, LocalDateTime inicio, LocalDateTime fin);

    /** Agenda: todas las citas en un rango. */
    List<CitaMedica> findByFechaHoraBetweenOrderByFechaHoraAsc(
            LocalDateTime inicio, LocalDateTime fin);

    /** Historial de citas de una mascota. */
    List<CitaMedica> findByMascotaIdOrderByFechaHoraDesc(Long mascotaId);
}
