-- ============================================================
-- CARGA INICIAL DE USUARIOS (auth-usuario)
-- Contrasenas encriptadas con BCrypt (BCryptPasswordEncoder, factor 10).
--
--   admin@veterinaria.com    -> Admin123*
--   vet@veterinaria.com      -> Vet123*
--   cliente@veterinaria.com  -> Cliente123*
--
-- Estas credenciales coinciden con las que usa el script de pruebas
-- (test-veterinaria.ps1 / test-veterinaria.sh).
-- ON CONFLICT evita errores si el script se ejecuta varias veces.
-- ============================================================

INSERT INTO usuarios (nombre, telefono, email, password, rol) VALUES
('Administrador', '555-0001', 'admin@veterinaria.com', '$2a$10$AGw8sg1MQmypQk/9bOaIi.hg5dgBwUOOJqhgDHYR..5c58TCaAK76', 'ADMIN')
ON CONFLICT (email) DO NOTHING;

INSERT INTO usuarios (nombre, telefono, email, password, rol) VALUES
('Dr. Veterinario', '555-0002', 'vet@veterinaria.com', '$2a$10$wDZ2vJVEwuX8Gy149nd7/utWRxhZBkNfi1NJZvDW4/LSUd1tK4H7.', 'VET')
ON CONFLICT (email) DO NOTHING;

INSERT INTO usuarios (nombre, telefono, email, password, rol) VALUES
('Cliente Ejemplo', '555-0003', 'cliente@veterinaria.com', '$2a$10$W9KZRBjr6F59c0AmYC3QRuVnfAth2nxQFw9b1jlTqzXJGeRBPwKIK', 'CLIENTE')
ON CONFLICT (email) DO NOTHING;
