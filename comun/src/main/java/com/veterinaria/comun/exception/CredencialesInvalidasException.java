package com.veterinaria.comun.exception;

/**
 * Credenciales incorrectas en el login. Se traduce a 401 Unauthorized.
 */
public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException(String message) {
        super(message);
    }
}
