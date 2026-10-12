package com.veterinaria.expediente.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Representa el expediente clinico de una cita.
 *
 * La relacion cita <-> expediente es UNICA: una cita tiene como maximo un expediente.
 * Se garantiza con la restriccion UNIQUE sobre "cita_id".
 *
 * "mascotaId" se guarda de forma redundante para poder consultar el historial de una
 * mascota sin tener que llamar a cita-service por cada expediente.
 */
@Entity
@Table(name = "expedientes",
        uniqueConstraints = @UniqueConstraint(name = "uk_expedientes_cita", columnNames = "cita_id"),
        indexes = @Index(name = "idx_expedientes_mascota", columnList = "mascota_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpedienteClinico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Referencia unica a la cita que origina el expediente. */
    @Column(name = "cita_id", nullable = false, unique = true)
    private Long citaId;

    /** Copia del id de la mascota (para historial eficiente). */
    @Column(name = "mascota_id", nullable = false)
    private Long mascotaId;

    @Column(name = "diagnostico", nullable = false, length = 1000)
    private String diagnostico;

    @Column(name = "tratamiento", nullable = false, length = 1000)
    private String tratamiento;

    @Column(name = "peso_kg", nullable = false)
    private Double pesoKg;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;
}
