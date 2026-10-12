package com.veterinaria.cita.enums;

/**
 * Estados posibles de una cita medica.
 */
public enum EstadoCita {
    /** Cita agendada y vigente. */
    PENDIENTE,
    /** La cita ya fue atendida y tiene expediente. */
    COMPLETADA,
    /** La cita fue cancelada (no ocupa horario). */
    CANCELADA
}
