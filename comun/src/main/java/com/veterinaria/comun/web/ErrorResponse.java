package com.veterinaria.comun.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato ESTANDARIZADO de todas las respuestas de error de la API.
 *
 * Ejemplo:
 * {
 *   "timestamp": "2026-01-15T10:00:00",
 *   "status": 409,
 *   "error": "Conflict",
 *   "message": "El veterinario ya tiene una cita en ese horario",
 *   "path": "/api/v1/citas",
 *   "detalles": { "fechaHora": "ya ocupado" }
 * }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> detalles) {

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path, null);
    }

    public static ErrorResponse of(int status, String error, String message, String path,
                                   Map<String, String> detalles) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path, detalles);
    }
}
