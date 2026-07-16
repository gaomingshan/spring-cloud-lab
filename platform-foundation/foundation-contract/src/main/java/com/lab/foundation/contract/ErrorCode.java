package com.lab.foundation.contract;

public record ErrorCode(String code, String message, ErrorCategory category, boolean retryable, int httpStatus) {
}
