package com.veterinaria.comun.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veterinaria.comun.web.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Protege los endpoints internos (/api/v1/internal/**).
 *
 * Estos endpoints son usados SOLO por otros microservicios (comunicacion entre servicios),
 * nunca por el cliente final. Por eso NO se protegen con JWT de usuario, sino con una
 * clave compartida enviada en el header "X-Internal-Key".
 *
 * NOTA DE SEGURIDAD: este esquema es aceptable para un proyecto academico en red local.
 * En produccion se usaria OAuth2/mTLS o una red privada entre servicios.
 */
@Component
public class InternalApiFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Internal-Key";

    private final String internalKey;
    private final ObjectMapper objectMapper;

    public InternalApiFilter(@Value("${internal.key}") String internalKey,
                             ObjectMapper objectMapper) {
        this.internalKey = internalKey;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Solo aplica a rutas internas.
        if (!path.startsWith("/api/v1/internal/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String provided = request.getHeader(HEADER);
        if (provided == null || !provided.equals(internalKey)) {
            ErrorResponse body = ErrorResponse.of(
                    HttpStatus.FORBIDDEN.value(),
                    HttpStatus.FORBIDDEN.getReasonPhrase(),
                    "Acceso interno denegado: clave de servicio invalida o ausente",
                    path);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), body);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
