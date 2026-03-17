package com.warehouse.common;

import com.warehouse.security.ForbiddenException;
import com.warehouse.security.RateLimitException;
import com.warehouse.security.UnauthorizedException;
import com.warehouse.stock.InsufficientStockException;
import com.warehouse.stock.StockLockException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public Result<Void> handleUnauthorized(UnauthorizedException ex) {
        return Result.error(401, ex.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public Result<Void> handleForbidden(ForbiddenException ex) {
        return Result.error(403, ex.getMessage());
    }

    @ExceptionHandler(RateLimitException.class)
    public Result<Void> handleRateLimit(RateLimitException ex) {
        return Result.error(429, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldError() == null
                ? "请求参数校验失败"
                : ex.getBindingResult().getFieldError().getDefaultMessage();
        return Result.error(400, message);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public Result<Void> handleInsufficientStock(InsufficientStockException ex) {
        return Result.error(409, ex.getMessage());
    }

    @ExceptionHandler(StockLockException.class)
    public Result<Void> handleStockLock(StockLockException ex) {
        return Result.error(503, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception ex) {
        return Result.error(500, ex.getMessage());
    }
}
