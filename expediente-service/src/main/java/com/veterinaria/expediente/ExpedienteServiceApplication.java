package com.veterinaria.expediente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del microservicio de expedientes clinicos.
 */
@SpringBootApplication(scanBasePackages = "com.veterinaria")
public class ExpedienteServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpedienteServiceApplication.class, args);
    }
}
