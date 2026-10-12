package com.veterinaria.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway del sistema veterinario.
 *
 * Punto de entrada UNICO en el puerto 8080. Recibe todas las peticiones y las
 * reenvia al microservicio correspondiente segun el prefijo de la ruta:
 *
 *   /api/v1/auth/**        -> auth-usuario       (8081)
 *   /api/v1/usuarios/**    -> auth-usuario       (8081)
 *   /api/v1/mascotas/**    -> mascota-service    (8082)
 *   /api/v1/citas/**       -> cita-service       (8083)
 *   /api/v1/expedientes/** -> expediente-service (8084)
 *
 * El enrutamiento NO usa Spring Cloud Gateway: se implementa con un filtro
 * servlet (ProxyFilter) que usa java.net.http.HttpClient. Asi no se necesita
 * ninguna dependencia nueva (el firewall bloquea descargas de Maven).
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
