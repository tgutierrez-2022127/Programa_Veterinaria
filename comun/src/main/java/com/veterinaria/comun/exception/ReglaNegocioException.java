package com.veterinaria.comun.exception;

/**
 * Excepcion base para errores de REGLA DE NEGOCIO (no de validacion de datos).
 * El GlobalExceptionHandler la traduce a 400 Bad Request.
 */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String message) {
        super(message);
    }
}
