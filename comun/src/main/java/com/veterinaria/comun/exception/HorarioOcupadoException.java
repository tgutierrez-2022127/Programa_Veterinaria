package com.veterinaria.comun.exception;

/**
 * REGLA 1: el veterinario ya tiene una cita en ese rango horario. Se traduce a 409 Conflict.
 */
public class HorarioOcupadoException extends RuntimeException {
    public HorarioOcupadoException(String message) {
        super(message);
    }
}
