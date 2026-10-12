package com.veterinaria.cita.client;

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
 * Cliente HTTP hacia auth-usuario (endpoint interno).
 * Permite validar que el veterinario existe y que realmente tiene rol VET.
 */
@Component
public class AuthClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String internalKey;

    public AuthClient(RestTemplate restTemplate,
                      @Value("${servicios.auth.url}") String baseUrl,
                      @Value("${internal.key}") String internalKey) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.internalKey = internalKey;
    }

    public UsuarioClienteDto obtenerUsuario(Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Key", internalKey);

        try {
            ResponseEntity<UsuarioClienteDto> resp = restTemplate.exchange(
                    baseUrl + "/api/v1/internal/usuarios/" + id,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    UsuarioClienteDto.class);
            return resp.getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new RecursoNoEncontradoException("El veterinario con id " + id + " no existe");
        } catch (Exception ex) {
            throw new ServicioExternoException(
                    "No se pudo consultar el usuario en auth-usuario: " + ex.getMessage());
        }
    }
}
