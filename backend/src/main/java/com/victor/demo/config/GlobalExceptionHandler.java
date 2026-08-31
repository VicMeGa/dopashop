package com.victor.demo.config;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final Map<Integer, String> SPANISH_ERRORS = Map.ofEntries(
            Map.entry(400, "Solicitud inválida"),
            Map.entry(401, "No autorizado"),
            Map.entry(403, "Acceso denegado"),
            Map.entry(404, "No encontrado"),
            Map.entry(405, "Método no permitido"),
            Map.entry(409, "Conflicto"),
            Map.entry(413, "Archivo demasiado grande"),
            Map.entry(415, "Tipo de contenido no soportado"),
            Map.entry(422, "Entidad no procesable"),
            Map.entry(500, "Error interno del servidor")
    );

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining(". "));
        if (message.isEmpty()) {
            message = "Los datos enviados no son válidos";
        }
        return build(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(jakarta.validation.ConstraintViolation::getMessage)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining(". "));
        if (message.isEmpty()) {
            message = "Los parámetros enviados no son válidos";
        }
        return build(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName() != null ? ex.getName() : "parámetro";
        return build(HttpStatus.BAD_REQUEST, "El valor del parámetro '" + name + "' no es válido");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        return build(HttpStatus.BAD_REQUEST, "Falta el parámetro obligatorio: " + ex.getParameterName());
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Map<String, Object>> handleMissingPart(MissingServletRequestPartException ex) {
        return build(HttpStatus.BAD_REQUEST, "Falta el archivo requerido: " + ex.getRequestPartName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "El cuerpo de la petición es inválido");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoResourceFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "La ruta solicitada no existe");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Método no permitido para esta ruta");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMediaType(HttpMediaTypeNotSupportedException ex) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de contenido no soportado");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Conflicto de integridad de datos: {}", ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.BAD_REQUEST, "Los datos enviados son inválidos o ya existen");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        String message = ex.getReason();
        if (message == null || message.isBlank()) {
            message = spanishError(ex.getStatusCode());
        }
        return build(ex.getStatusCode(), message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Error no controlado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado");
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatusCode status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("error", spanishError(status));
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    private String spanishError(HttpStatusCode status) {
        return SPANISH_ERRORS.getOrDefault(status.value(), "Error");
    }
}