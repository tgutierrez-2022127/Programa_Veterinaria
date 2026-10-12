package com.veterinaria.comun.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro que se ejecuta UNA vez por peticion.
 *
 * Responsabilidad:
 *   1. Leer el header "Authorization: Bearer <token>".
 *   2. Validar el token.
 *   3. Colocar la autenticacion en el SecurityContext.
 *
 * Si no hay token, o es invalido/expirado, simplemente NO autentica y deja seguir
 * la cadena. Los endpoints protegidos responderan 401/403 segun corresponda.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // Sin header Bearer -> la peticion sigue sin autenticar.
        if (header == null || !header.startsWith(PREFIJO)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(PREFIJO.length());

        try {
            // Si el token esta expirado o mal firmado, esto lanza una excepcion.
            AuthenticatedUser usuario = jwtService.convertirEnUsuario(token);

            // Spring Security trabaja con "authorities". El prefijo ROLE_ es el estandar
            // para poder usar hasRole('ADMIN') / @PreAuthorize("hasRole('VET')").
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + usuario.rol()));

            var authentication = new UsernamePasswordAuthenticationToken(usuario, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception ex) {
            // Token invalido/expirado: limpiamos el contexto. No lanzamos error aqui
            // para dejar que el EntryPoint produzca el 401 con formato estandar.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
