package com.veterinaria.comun.exception;

/**
 * El usuario autenticado no tiene permiso sobre el recurso (por ejemplo, un CLIENTE
 * intentando ver mascotas de otro cliente). Se traduce a 403 Forbidden.
 */
public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String message) {
        super(message);
    }
}
