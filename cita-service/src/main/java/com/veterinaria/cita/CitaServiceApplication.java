package com.veterinaria.cita;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del microservicio de citas.
 * Aqui se implementan las reglas de negocio mas importantes del sistema.
 */
@SpringBootApplication(scanBasePackages = "com.veterinaria")
public class CitaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CitaServiceApplication.class, args);
    }
}
