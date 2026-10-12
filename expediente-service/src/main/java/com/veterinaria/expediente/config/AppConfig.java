package com.veterinaria.expediente.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;

/**
 * Cliente HTTP para comunicacion entre microservicios.
 *
 * IMPORTANTE: usamos JdkClientHttpRequestFactory (java.net.http.HttpClient) en lugar
 * del RestTemplate por defecto. El RestTemplate por defecto usa HttpURLConnection, que
 * NO soporta el metodo HTTP PATCH (lanza ProtocolException). Como expediente-service
 * hace PATCH a cita-service (/completar), necesitamos esta factoria.
 */
@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(new JdkClientHttpRequestFactory(HttpClient.newHttpClient()));
    }
}
