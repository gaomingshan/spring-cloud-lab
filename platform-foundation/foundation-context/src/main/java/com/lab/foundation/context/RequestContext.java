package com.lab.foundation.context;

public record RequestContext(String requestId, String traceId, String spanId, String tenantId, String principalId) {
}
