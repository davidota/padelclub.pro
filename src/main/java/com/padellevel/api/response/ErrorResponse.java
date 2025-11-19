package com.padellevel.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Respuesta de error estándar de la API.
 * Se utiliza para comunicar errores de forma consistente.
 */
@Schema(description = "Respuesta de error de la API")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @Schema(description = "Indica que hubo un error", example = "false")
    private boolean success = false;

    @Schema(description = "Código de error HTTP", example = "400")
    private int status;

    @Schema(description = "Mensaje de error", example = "Error de validación")
    private String message;

    @Schema(description = "Detalles adicionales del error")
    private String details;

    @Schema(description = "Timestamp del error")
    private LocalDateTime timestamp;

    @Schema(description = "Ruta de la petición que causó el error", example = "/api/v1/torneos")
    private String path;

    @Schema(description = "Errores de validación de campos")
    private Map<String, String> fieldErrors;

    @Schema(description = "Lista de errores múltiples")
    private List<String> errors;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String message) {
        this();
        this.status = status;
        this.message = message;
    }

    public ErrorResponse(int status, String message, String details) {
        this();
        this.status = status;
        this.message = message;
        this.details = details;
    }

    public ErrorResponse(int status, String message, String path, String details) {
        this();
        this.status = status;
        this.message = message;
        this.path = path;
        this.details = details;
    }

    // Getters y Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
