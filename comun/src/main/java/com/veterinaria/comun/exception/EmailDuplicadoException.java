package com.veterinaria.comun.exception;

/**
 * Se lanza cuando se intenta registrar un email que ya existe. Se traduce a 409 Conflict.
 */
public class EmailDuplicadoException extends RuntimeException {
    public EmailDuplicadoException(String message) {
        super(message);
    }
}
