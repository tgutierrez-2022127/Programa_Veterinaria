package com.veterinaria.comun.exception;

/**
 * Se lanza cuando un microservicio no puede comunicarse con otro (por ejemplo,
 * cita-service no logra consultar mascota-service). Se traduce a 503 Service Unavailable.
 */
public class ServicioExternoException extends RuntimeException {
    public ServicioExternoException(String message) {
        super(message);
    }
}
