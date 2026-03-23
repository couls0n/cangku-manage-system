package com.warehouse.common;

import com.warehouse.security.ForbiddenException;
import com.warehouse.security.RateLimitException;
import com.warehouse.security.UnauthorizedException;
import com.warehouse.stock.InsufficientStockException;
import com.warehouse.stock.StockLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Result<Void>> handleUnauthorized(UnauthorizedException ex) {
        return build(HttpStatus.UNAUTHORIZED, 401, ex.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Result<Void>> handleForbidden(ForbiddenException ex) {
        return build(HttpStatus.FORBIDDEN, 403, ex.getMessage());
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<Result<Void>> handleRateLimit(RateLimitException ex) {
        return build(HttpStatus.TOO_MANY_REQUESTS, 429, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldError() == null
                ? "Request validation failed"
                : ex.getBindingResult().getFieldError().getDefaultMessage();
        return build(HttpStatus.BAD_REQUEST, 400, message);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Result<Void>> handleInsufficientStock(InsufficientStockException ex) {
        return build(HttpStatus.CONFLICT, 409, ex.getMessage());
    }

    @ExceptionHandler(StockLockException.class)
    public ResponseEntity<Result<Void>> handleStockLock(StockLockException ex) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, 503, ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Result<Void>> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, 404, ex.getMessage());
    }

    @ExceptionHandler(OperationNotAllowedException.class)
    public ResponseEntity<Result<Void>> handleOperationNotAllowed(OperationNotAllowedException ex) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, 405, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, 400, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleOther(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, 500, "Internal server error");
    }

    private ResponseEntity<Result<Void>> build(HttpStatus status, int code, String message) {
        return ResponseEntity.status(status).body(Result.error(code, message));
    }
}
