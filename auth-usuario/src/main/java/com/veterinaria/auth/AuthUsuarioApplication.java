package com.veterinaria.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del microservicio de autenticacion y usuarios.
 *
 * scanBasePackages = "com.veterinaria" es NECESARIO para que Spring detecte los
 * beans de la libreria compartida (JWT, filtros de seguridad, manejo de excepciones)
 * que viven en el paquete com.veterinaria.comun.
 */
@SpringBootApplication(scanBasePackages = "com.veterinaria")
public class AuthUsuarioApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthUsuarioApplication.class, args);
    }
}
