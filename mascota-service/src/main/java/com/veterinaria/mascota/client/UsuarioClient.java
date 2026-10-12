package com.veterinaria.mascota.client;

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
 * Cliente HTTP que consulta auth-usuario.
 *
 * Se usa para validar que un cliente existe antes de asociarle una mascota.
 * Envia el header X-Internal-Key para acceder al endpoint interno.
 */
@Component
public class UsuarioClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String internalKey;

    public UsuarioClient(RestTemplate restTemplate,
                         @Value("${servicios.auth.url}") String baseUrl,
                         @Value("${internal.key}") String internalKey) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.internalKey = internalKey;
    }

    /** Obtiene un usuario por id. Lanza 404 si no existe, 503 si auth-usuario no responde. */
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
            throw new RecursoNoEncontradoException("El cliente con id " + id + " no existe");
        } catch (Exception ex) {
            throw new ServicioExternoException(
                    "No se pudo validar el cliente con el servicio auth-usuario: " + ex.getMessage());
        }
    }
}
