package com.lab.foundation.contract;

public record ApiError(String code, String message, ErrorCategory category, boolean retryable) {
}
