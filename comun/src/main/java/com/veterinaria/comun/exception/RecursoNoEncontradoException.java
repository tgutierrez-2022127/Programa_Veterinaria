package com.veterinaria.comun.exception;

/**
 * Se lanza cuando un recurso solicitado no existe. Se traduce a 404 Not Found.
 */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
