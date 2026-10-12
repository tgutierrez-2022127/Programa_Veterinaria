package com.veterinaria.auth.enums;

/**
 * Roles disponibles en el sistema.
 * Se guardan en la base de datos como texto (@Enumerated(EnumType.STRING))
 * para que sean legibles y no dependan del orden de declaracion.
 */
public enum Rol {
    ADMIN,
    VET,
    CLIENTE
}
