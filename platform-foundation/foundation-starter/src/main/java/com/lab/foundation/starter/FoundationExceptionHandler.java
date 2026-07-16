package com.lab.foundation.starter;

import com.lab.foundation.context.RequestContextHolder;
import com.lab.foundation.contract.ApiError;
import com.lab.foundation.contract.ApiResponse;
import com.lab.foundation.contract.BusinessException;
import com.lab.foundation.contract.ErrorCategory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class FoundationExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException exception) {
        var code = exception.errorCode();
        return ResponseEntity.status(code.httpStatus()).body(ApiResponse.failure(
                new ApiError(code.code(), code.message(), code.category(), code.retryable()), traceId()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.failure(
                new ApiError("VALIDATION_ERROR", "Request validation failed", ErrorCategory.VALIDATION, false), traceId()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.failure(
                new ApiError("INTERNAL_ERROR", "Internal server error", ErrorCategory.SYSTEM, true), traceId()));
    }

    private String traceId() {
        var context = RequestContextHolder.get();
        return context == null ? null : context.traceId();
    }
}
