package com.veterinaria.mascota.entity;

import com.veterinaria.mascota.enums.Especie;
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

/**
 * Representa una mascota, que pertenece a un usuario CLIENTE.
 *
 * NOTA DE ARQUITECTURA: en microservicios, el cliente vive en la base de datos de
 * auth-usuario. Por eso NO usamos @ManyToOne (seria otra base de datos). Guardamos
 * el identificador "clienteId" y validamos su existencia llamando a auth-usuario.
 * (Esta es la diferencia clave frente a un monolito, donde si usariamos @ManyToOne).
 */
@Entity
@Table(name = "mascotas", indexes = @Index(name = "idx_mascotas_cliente", columnList = "cliente_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "especie", nullable = false, length = 20)
    private Especie especie;

    @Column(name = "raza", length = 100)
    private String raza;

    @Column(name = "edad")
    private Integer edad;

    /** Id del usuario CLIENTE dueño de la mascota (referencia al servicio auth-usuario). */
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;
}
