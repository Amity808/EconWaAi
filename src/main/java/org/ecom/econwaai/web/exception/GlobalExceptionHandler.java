package org.ecom.econwaai.web.exception;

import org.ecom.econwaai.common.context.OrgContextHolder;
import org.ecom.econwaai.common.exception.AppException;
import org.ecom.econwaai.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex) {
        String correlationId = OrgContextHolder.getCorrelationId().orElse("unknown");
        log.warn("AppException occurred [code={}, correlationId={}]: {}", ex.getErrorCode().getCode(), correlationId, ex.getMessage());

        ErrorResponse response = ErrorResponse.of(
                ex.getErrorCode().getCode(),
                ex.getMessage(),
                ex.getHttpStatus(),
                correlationId,
                ex.getDetails()
        );
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        String correlationId = OrgContextHolder.getCorrelationId().orElse("unknown");
        log.warn("Access denied [correlationId={}]: {}", correlationId, ex.getMessage());

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.FORBIDDEN.getCode(),
                "Access is denied to the requested resource",
                HttpStatus.FORBIDDEN.value(),
                correlationId,
                Map.of()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        String correlationId = OrgContextHolder.getCorrelationId().orElse("unknown");
        log.warn("Authentication failed [correlationId={}]: {}", correlationId, ex.getMessage());

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.UNAUTHORIZED.getCode(),
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                correlationId,
                Map.of()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        String correlationId = OrgContextHolder.getCorrelationId().orElse("unknown");
        Map<String, Object> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.VALIDATION_ERROR.getCode(),
                "Validation failed for one or more fields",
                HttpStatus.BAD_REQUEST.value(),
                correlationId,
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandledException(Exception ex) {
        String correlationId = OrgContextHolder.getCorrelationId().orElse("unknown");
        log.error("Unhandled server exception [correlationId={}]", correlationId, ex);

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INTERNAL_ERROR.getCode(),
                "An unexpected internal error occurred. Please quote correlation ID",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                correlationId,
                Map.of()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
