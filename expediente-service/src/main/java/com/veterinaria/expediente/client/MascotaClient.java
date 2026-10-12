package com.veterinaria.expediente.client;

import com.veterinaria.comun.exception.RecursoNoEncontradoException;
import com.veterinaria.comun.exception.ServicioExternoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Cliente HTTP hacia mascota-service.
 * Se usa para verificar que una mascota pertenece al CLIENTE antes de mostrar su
 * historial de expedientes (seguridad).
 */
@Component
public class MascotaClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String internalKey;

    public MascotaClient(RestTemplate restTemplate,
                         @Value("${servicios.mascota.url}") String baseUrl,
                         @Value("${internal.key}") String internalKey) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.internalKey = internalKey;
    }

    public MascotaClienteDto obtenerMascota(Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Key", internalKey);
        try {
            ResponseEntity<MascotaClienteDto> resp = restTemplate.exchange(
                    baseUrl + "/api/v1/internal/mascotas/" + id,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    MascotaClienteDto.class);
            return resp.getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new RecursoNoEncontradoException("La mascota con id " + id + " no existe");
        } catch (Exception ex) {
            throw new ServicioExternoException(
                    "No se pudo consultar la mascota en mascota-service: " + ex.getMessage());
        }
    }
}
