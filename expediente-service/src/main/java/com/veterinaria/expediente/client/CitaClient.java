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
 * Cliente HTTP hacia cita-service:
 *  - obtener una cita (para saber a que mascota pertenece).
 *  - marcarla como COMPLETADA al registrar su expediente.
 */
@Component
public class CitaClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String internalKey;

    public CitaClient(RestTemplate restTemplate,
                      @Value("${servicios.cita.url}") String baseUrl,
                      @Value("${internal.key}") String internalKey) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.internalKey = internalKey;
    }

    public CitaClienteDto obtenerCita(Long citaId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Key", internalKey);
        try {
            ResponseEntity<CitaClienteDto> resp = restTemplate.exchange(
                    baseUrl + "/api/v1/internal/citas/" + citaId,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    CitaClienteDto.class);
            return resp.getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new RecursoNoEncontradoException("La cita con id " + citaId + " no existe");
        } catch (Exception ex) {
            throw new ServicioExternoException(
                    "No se pudo consultar la cita en cita-service: " + ex.getMessage());
        }
    }

    public void completarCita(Long citaId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Key", internalKey);
        try {
            restTemplate.exchange(
                    baseUrl + "/api/v1/internal/citas/" + citaId + "/completar",
                    HttpMethod.PATCH,
                    new HttpEntity<>(headers),
                    CitaClienteDto.class);
        } catch (Exception ex) {
            throw new ServicioExternoException(
                    "No se pudo completar la cita en cita-service: " + ex.getMessage());
        }
    }
}
