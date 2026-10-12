package com.veterinaria.comun.exception;

/**
 * REGLA 3: la cita no puede cancelarse porque faltan 2 horas o menos. Se traduce a 400 Bad Request.
 */
public class CancelacionNoPermitidaException extends RuntimeException {
    public CancelacionNoPermitidaException(String message) {
        super(message);
    }
}
