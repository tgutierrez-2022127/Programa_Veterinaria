package com.veterinaria.mascota.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;

/**
 * Configuracion de cliente HTTP para comunicacion entre microservicios.
 * Usamos RestTemplate con JdkClientHttpRequestFactory (java.net.http.HttpClient),
 * que soporta todos los verbos HTTP (GET, POST, PUT, PATCH, DELETE).
 */
@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(new JdkClientHttpRequestFactory(HttpClient.newHttpClient()));
    }
}
