package com.veterinaria.mascota;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del microservicio de mascotas.
 * scanBasePackages incluye "com.veterinaria" para cargar los beans compartidos (comun).
 */
@SpringBootApplication(scanBasePackages = "com.veterinaria")
public class MascotaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MascotaServiceApplication.class, args);
    }
}
