package com.lab.reliable.message.store;

import com.lab.reliable.message.model.DeliveryMode;
import com.lab.reliable.message.model.OutboxMessage;
import com.lab.reliable.message.model.ProcessingClaim;

import java.time.Instant;
import java.util.List;

public interface ReliableMessageStore {
    void appendOutbox(String messageId, String eventType, String payload, DeliveryMode deliveryMode, Instant now);

    List<OutboxMessage> claimOutbox(int limit, Instant now, Instant leaseUntil);

    void markOutboxSent(String messageId, Instant sentAt);

    void markOutboxRetry(String messageId, Instant nextAttemptAt, String error);

    void markOutboxDead(String messageId, String error);

    int recoverExpiredOutbox(Instant now);

    ProcessingClaim claimProcessing(String consumerScope, String fingerprint, String eventType,
                                    Instant now, Instant leaseUntil);

    void markProcessingSucceeded(String consumerScope, String fingerprint, Instant completedAt);

    void markProcessingFailed(String consumerScope, String fingerprint, String error, Instant failedAt);
}
