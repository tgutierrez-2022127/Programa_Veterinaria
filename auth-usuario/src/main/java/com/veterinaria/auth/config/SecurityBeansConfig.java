package com.veterinaria.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Beans de autenticacion especificos del servicio de usuarios.
 *
 * El SecurityFilterChain (reglas de rutas, filtros JWT) vive en la libreria "comun".
 * Aqui solo definimos como se cifra la contrasena y como se construye el
 * AuthenticationManager, que es propio de este servicio porque es el unico que
 * valida usuario + contrasena contra la base de datos.
 */
@Configuration
public class SecurityBeansConfig {

    /**
     * BCrypt es un algoritmo de hash con "salt" incorporado y factor de coste.
     * Es ideal para contrasenas porque es lento a proposito (dificulta ataques de fuerza bruta).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager es el que ejecuta el proceso de autenticacion:
     * recibe email+password, busca el UserDetailsService, compara el hash y,
     * si coincide, devuelve un Authentication.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
