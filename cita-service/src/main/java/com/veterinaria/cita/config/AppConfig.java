package com.veterinaria.cita.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;

/**
 * Cliente HTTP para comunicacion entre microservicios.
 *
 * Usamos JdkClientHttpRequestFactory (java.net.http.HttpClient) para poder usar
 * cualquier verbo HTTP, incluido PATCH, si en el futuro se necesita.
 */
@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(new JdkClientHttpRequestFactory(HttpClient.newHttpClient()));
    }
}
