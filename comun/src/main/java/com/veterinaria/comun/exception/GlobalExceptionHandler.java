package com.veterinaria.comun.exception;

import com.veterinaria.comun.web.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejador GLOBAL de excepciones.
 *
 * @RestControllerAdvice intercepta las excepciones lanzadas por CUALQUIER controller
 * y las transforma en una respuesta JSON con el formato de {@link ErrorResponse}.
 *
 * Esto evita repetir try/catch en cada endpoint y garantiza respuestas de error
 * consistentes en toda la API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---------- 404 NOT FOUND ----------
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleNoEncontrado(RecursoNoEncontradoException ex,
                                                            HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    // ---------- 401 UNAUTHORIZED ----------
    @ExceptionHandler({CredencialesInvalidasException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleCredenciales(RuntimeException ex,
                                                            HttpServletRequest req) {
        String msg = (ex instanceof BadCredentialsException)
                ? "Credenciales incorrectas"
                : ex.getMessage();
        return build(HttpStatus.UNAUTHORIZED, msg, req);
    }

    // ---------- 403 FORBIDDEN ----------
    @ExceptionHandler({AccesoDenegadoException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleAcceso(RuntimeException ex,
                                                      HttpServletRequest req) {
        String msg = (ex instanceof AccessDeniedException)
                ? "No tiene permisos para realizar esta operacion"
                : ex.getMessage();
        return build(HttpStatus.FORBIDDEN, msg, req);
    }

    // ---------- 409 CONFLICT (conflictos de negocio: duplicados, horario, limite) ----------
    @ExceptionHandler({EmailDuplicadoException.class, HorarioOcupadoException.class,
            LimiteCitasException.class})
    public ResponseEntity<ErrorResponse> handleConflictos(RuntimeException ex,
                                                          HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    // ---------- 400 BAD REQUEST (reglas de negocio y fechas invalidas) ----------
    @ExceptionHandler({ReglaNegocioException.class, CancelacionNoPermitidaException.class,
            IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleReglaNegocio(RuntimeException ex,
                                                            HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    // ---------- 400: validacion de DTOs (@Valid) ----------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacion(MethodArgumentNotValidException ex,
                                                          HttpServletRequest req) {
        Map<String, String> detalles = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            detalles.put(error.getField(), error.getDefaultMessage());
        }
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Error de validacion en los datos enviados",
                req.getRequestURI(),
                detalles);
        return ResponseEntity.badRequest().body(body);
    }

    // ---------- 400: JSON mal formado o tipo incorrecto ----------
    @ExceptionHandler({HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleJsonInvalido(Exception ex,
                                                            HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST,
                "El cuerpo de la peticion es invalido o tiene un formato incorrecto", req);
    }

    // ---------- 503: fallo al comunicarse con otro microservicio ----------
    @ExceptionHandler(ServicioExternoException.class)
    public ResponseEntity<ErrorResponse> handleServicioExterno(ServicioExternoException ex,
                                                               HttpServletRequest req) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), req);
    }

    // ---------- 500: cualquier error no controlado ----------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrio un error interno: " + ex.getMessage(), req);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message,
                                                HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(
                status.value(), status.getReasonPhrase(), message, req.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
