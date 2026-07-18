package com.lab.message.contract;

public record PublishResult(
        String eventId,
        PublishStatus status,
        String messageId,
        String failureReason
) {
}
