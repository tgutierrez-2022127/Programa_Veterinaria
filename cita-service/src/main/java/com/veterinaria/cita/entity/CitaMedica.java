package com.veterinaria.cita.entity;

import com.veterinaria.cita.enums.EstadoCita;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Representa una cita medica.
 *
 * Duracion FIJA de 30 minutos (regla del sistema). Guardamos la hora de INICIO en
 * "fechaHora"; la hora de fin se calcula como fechaHora + 30 minutos.
 *
 * "clienteId" se guarda de forma redundante (denormalizada) a proposito: permite
 * verificar la Regla 2 (maximo 2 citas pendientes por dia) sin llamar a otros servicios.
 */
@Entity
@Table(name = "citas", indexes = {
        @Index(name = "idx_citas_veterinario", columnList = "veterinario_id"),
        @Index(name = "idx_citas_cliente", columnList = "cliente_id"),
        @Index(name = "idx_citas_fecha", columnList = "fecha_hora")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mascota_id", nullable = false)
    private Long mascotaId;

    @Column(name = "veterinario_id", nullable = false)
    private Long veterinarioId;

    /** Dueño de la mascota. Se copia para poder validar la Regla 2 de forma eficiente. */
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    /** Hora de INICIO de la cita. */
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "motivo", nullable = false, length = 255)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoCita estado;
}
