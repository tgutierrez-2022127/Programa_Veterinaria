package com.veterinaria.auth.enums;

/**
 * Roles disponibles en el sistema.
 * ADMIN   → control total de la clínica
 * VET     → veterinario, gestiona citas y expedientes
 * CLIENTE → dueño de mascotas, solicita citas
 */
public enum Rol {
    ADMIN,
    VET,
    CLIENTE
}