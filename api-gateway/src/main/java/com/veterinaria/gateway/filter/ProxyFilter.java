package com.veterinaria.gateway.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Proxy inverso implementado con Spring Web puro (sin Spring Cloud Gateway).
 *
 * Como funciona:
 *  1. Mira la ruta de la peticion entrante.
 *  2. Busca el microservicio destino segun el prefijo.
 *  3. Reenvia metodo, ruta, query string, cabeceras y cuerpo tal cual.
 *  4. Copia la respuesta (codigo de estado, Content-Type y cuerpo) de vuelta.
 *
 * De esta forma TODOS los endpoints de los 4 microservicios quedan accesibles
 * por un unico puerto (8080), que es lo que espera el script de pruebas.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ProxyFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ProxyFilter.class);

    /**
     * Cabeceras que NO se deben reenviar (son propias de la conexion HTTP).
     * HttpClient calcula Host y Content-Length por su cuenta; el resto son
     * cabeceras "hop-by-hop" de HTTP/1.1. Tambien se omite accept-encoding
     * para que el microservicio responda sin comprimir (y poder copiar el cuerpo).
     */
    private static final Set<String> CABECERAS_EXCLUIDAS = Set.of(
            "host", "connection", "content-length", "transfer-encoding",
            "keep-alive", "proxy-authenticate", "proxy-authorization",
            "te", "trailer", "upgrade", "accept-encoding",
            // "expect" (Expect: 100-continue) esta RESTRINGIDA por java.net.http:
            // si se intenta copiar, el cliente lanza IllegalArgumentException.
            "expect");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /** Prefijo de ruta -> URL base del microservicio. */
    private final Map<String, String> rutas = new LinkedHashMap<>();

    public ProxyFilter() {
        rutas.put("/api/v1/auth", "http://localhost:8081");
        rutas.put("/api/v1/usuarios", "http://localhost:8081");
        rutas.put("/api/v1/mascotas", "http://localhost:8082");
        rutas.put("/api/v1/citas", "http://localhost:8083");
        rutas.put("/api/v1/expedientes", "http://localhost:8084");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String ruta = request.getRequestURI();
        String destino = resolverDestino(ruta);

        // Si no pertenece a ningun microservicio, se deja pasar (Spring devuelve 404).
        if (destino == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String query = request.getQueryString();
            URI uri = URI.create(destino + ruta + (query != null ? "?" + query : ""));

            byte[] cuerpo = leerCuerpo(request);

            HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(30));

            copiarCabeceras(request, builder);

            builder.method(request.getMethod(), cuerpo.length > 0
                    ? HttpRequest.BodyPublishers.ofByteArray(cuerpo)
                    : HttpRequest.BodyPublishers.noBody());

            log.info("GATEWAY {} {} -> {}", request.getMethod(), ruta, destino);

            HttpResponse<byte[]> upstream = httpClient.send(builder.build(),
                    HttpResponse.BodyHandlers.ofByteArray());

            response.setStatus(upstream.statusCode());
            upstream.headers().firstValue("Content-Type")
                    .ifPresent(ct -> response.setHeader("Content-Type", ct));

            byte[] cuerpoRespuesta = upstream.body();
            if (cuerpoRespuesta != null && cuerpoRespuesta.length > 0) {
                response.getOutputStream().write(cuerpoRespuesta);
            }
            response.flushBuffer();

        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("GATEWAY error al contactar {}: {}", destino, ex.getMessage());
            escribirError(response, HttpServletResponse.SC_BAD_GATEWAY,
                    "No se pudo contactar el microservicio de destino: " + destino);
        }
    }

    /** Devuelve la URL base del microservicio cuyo prefijo coincide con la ruta, o null. */
    private String resolverDestino(String ruta) {
        for (Map.Entry<String, String> entrada : rutas.entrySet()) {
            String prefijo = entrada.getKey();
            if (ruta.equals(prefijo) || ruta.startsWith(prefijo + "/")) {
                return entrada.getValue();
            }
        }
        return null;
    }

    private byte[] leerCuerpo(HttpServletRequest request) throws IOException {
        try (InputStream in = request.getInputStream()) {
            return in.readAllBytes();
        }
    }

    private void copiarCabeceras(HttpServletRequest request, HttpRequest.Builder builder) {
        Enumeration<String> nombres = request.getHeaderNames();
        while (nombres.hasMoreElements()) {
            String nombre = nombres.nextElement();
            if (CABECERAS_EXCLUIDAS.contains(nombre.toLowerCase())) {
                continue;
            }
            Enumeration<String> valores = request.getHeaders(nombre);
            while (valores.hasMoreElements()) {
                String valor = valores.nextElement();
                try {
                    builder.header(nombre, valor);
                } catch (IllegalArgumentException ex) {
                    // Cabecera restringida por java.net.http (p. ej. expect): se ignora.
                    log.debug("Cabecera ignorada por restriccion del cliente HTTP: {}", nombre);
                }
            }
        }
    }

    private void escribirError(HttpServletResponse response, int estado, String mensaje)
            throws IOException {
        response.setStatus(estado);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String json = "{\"status\":" + estado
                + ",\"error\":\"Bad Gateway\""
                + ",\"mensaje\":\"" + mensaje.replace("\"", "'") + "\"}";
        response.getWriter().write(json);
    }
}
