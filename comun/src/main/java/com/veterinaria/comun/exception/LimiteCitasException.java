package com.veterinaria.comun.exception;

/**
 * REGLA 2: el cliente ya tiene 2 citas PENDIENTES para el mismo dia. Se traduce a 409 Conflict.
 */
public class LimiteCitasException extends RuntimeException {
    public LimiteCitasException(String message) {
        super(message);
    }
}
