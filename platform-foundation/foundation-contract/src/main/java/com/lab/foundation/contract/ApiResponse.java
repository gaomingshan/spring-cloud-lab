package com.lab.foundation.contract;

import java.time.Instant;

public record ApiResponse<T>(boolean success, T data, ApiError error, String traceId, Instant timestamp) {

    public static <T> ApiResponse<T> success(T data, String traceId) {
        return new ApiResponse<>(true, data, null, traceId, Instant.now());
    }

    public static <T> ApiResponse<T> failure(ApiError error, String traceId) {
        return new ApiResponse<>(false, null, error, traceId, Instant.now());
    }
}
