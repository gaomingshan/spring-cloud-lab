package com.lab.reliable.message.store;

import com.lab.reliable.message.model.DeliveryMode;
import com.lab.reliable.message.model.OutboxMessage;
import com.lab.reliable.message.model.OutboxStatus;
import com.lab.reliable.message.model.ProcessingClaim;
import com.lab.reliable.message.model.ProcessingStatus;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

public final class JdbcReliableMessageStore implements ReliableMessageStore {
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate requiresNew;

    public JdbcReliableMessageStore(JdbcTemplate jdbcTemplate, PlatformTransactionManager transactionManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.requiresNew = new TransactionTemplate(transactionManager);
        this.requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public void appendOutbox(String messageId, String eventType, String payload, DeliveryMode deliveryMode, Instant now) {
        jdbcTemplate.update("""
                insert into reliable_message_outbox
                    (message_id, event_type, payload, delivery_mode, status, attempt_count, next_attempt_at, created_at, updated_at)
                values (?, ?, ?, ?, 'PENDING', 0, ?, ?, ?)
                """, messageId, eventType, payload, deliveryMode.name(), timestamp(now), timestamp(now), timestamp(now));
    }

    @Override
    public List<OutboxMessage> claimOutbox(int limit, Instant now, Instant leaseUntil) {
        return requiresNew.execute(status -> {
            List<OutboxMessage> messages = jdbcTemplate.query("""
                    select message_id, event_type, payload, delivery_mode, status, attempt_count, next_attempt_at, locked_until, last_error
                    from reliable_message_outbox
                    where status = 'PENDING' and next_attempt_at <= ?
                    order by id
                    limit ? for update skip locked
                    """, (rs, rowNum) -> new OutboxMessage(
                    rs.getString("message_id"), rs.getString("event_type"), rs.getString("payload"),
                    DeliveryMode.valueOf(rs.getString("delivery_mode")), OutboxStatus.valueOf(rs.getString("status")),
                    rs.getInt("attempt_count"), rs.getTimestamp("next_attempt_at").toInstant(),
                    rs.getTimestamp("locked_until") == null ? null : rs.getTimestamp("locked_until").toInstant(),
                    rs.getString("last_error")), timestamp(now), limit);
            for (OutboxMessage message : messages) {
                jdbcTemplate.update("""
                        update reliable_message_outbox
                        set status = 'SENDING', attempt_count = attempt_count + 1, locked_until = ?, updated_at = ?
                        where message_id = ? and status = 'PENDING'
                        """, timestamp(leaseUntil), timestamp(now), message.messageId());
            }
            return messages.stream().map(message -> new OutboxMessage(
                    message.messageId(), message.eventType(), message.payload(), message.deliveryMode(), OutboxStatus.SENDING,
                    message.attemptCount() + 1, message.nextAttemptAt(), leaseUntil, message.lastError())).toList();
        });
    }

    @Override
    public void markOutboxSent(String messageId, Instant sentAt) {
        jdbcTemplate.update("""
                update reliable_message_outbox
                set status = 'SENT', locked_until = null, sent_at = ?, updated_at = ?
                where message_id = ? and status = 'SENDING'
                """, timestamp(sentAt), timestamp(sentAt), messageId);
    }

    @Override
    public void markOutboxRetry(String messageId, Instant nextAttemptAt, String error) {
        jdbcTemplate.update("""
                update reliable_message_outbox
                set status = 'PENDING', locked_until = null, next_attempt_at = ?, last_error = ?, updated_at = ?
                where message_id = ? and status = 'SENDING'
                """, timestamp(nextAttemptAt), error, timestamp(Instant.now()), messageId);
    }

    @Override
    public void markOutboxDead(String messageId, String error) {
        jdbcTemplate.update("""
                update reliable_message_outbox
                set status = 'DEAD', locked_until = null, last_error = ?, updated_at = ?
                where message_id = ? and status = 'SENDING'
                """, error, timestamp(Instant.now()), messageId);
    }

    @Override
    public int recoverExpiredOutbox(Instant now) {
        return jdbcTemplate.update("""
                update reliable_message_outbox
                set status = 'PENDING', locked_until = null, next_attempt_at = ?, updated_at = ?
                where status = 'SENDING' and locked_until < ?
                """, timestamp(now), timestamp(now), timestamp(now));
    }

    @Override
    public ProcessingClaim claimProcessing(String consumerScope, String fingerprint, String eventType,
                                           Instant now, Instant leaseUntil) {
        return requiresNew.execute(status -> {
            try {
                jdbcTemplate.update("""
                        insert into reliable_message_processing
                            (consumer_scope, fingerprint, event_type, status, attempt_count, lease_until, created_at, updated_at)
                        values (?, ?, ?, 'PROCESSING', 1, ?, ?, ?)
                        """, consumerScope, fingerprint, eventType, timestamp(leaseUntil), timestamp(now), timestamp(now));
                return ProcessingClaim.CLAIMED;
            } catch (DuplicateKeyException ignored) {
                String processingStatus = jdbcTemplate.queryForObject("""
                        select status from reliable_message_processing
                        where consumer_scope = ? and fingerprint = ? for update
                        """, String.class, consumerScope, fingerprint);
                if (ProcessingStatus.SUCCEEDED.name().equals(processingStatus)) {
                    return ProcessingClaim.ALREADY_SUCCEEDED;
                }
                int updated = jdbcTemplate.update("""
                        update reliable_message_processing
                        set status = 'PROCESSING', attempt_count = attempt_count + 1, lease_until = ?, updated_at = ?, last_error = null
                        where consumer_scope = ? and fingerprint = ?
                          and (status = 'FAILED' or lease_until < ?)
                        """, timestamp(leaseUntil), timestamp(now), consumerScope, fingerprint, timestamp(now));
                return updated == 1 ? ProcessingClaim.CLAIMED : ProcessingClaim.IN_PROGRESS;
            }
        });
    }

    @Override
    public void markProcessingSucceeded(String consumerScope, String fingerprint, Instant completedAt) {
        jdbcTemplate.update("""
                update reliable_message_processing
                set status = 'SUCCEEDED', lease_until = null, completed_at = ?, updated_at = ?, last_error = null
                where consumer_scope = ? and fingerprint = ? and status = 'PROCESSING'
                """, timestamp(completedAt), timestamp(completedAt), consumerScope, fingerprint);
    }

    @Override
    public void markProcessingFailed(String consumerScope, String fingerprint, String error, Instant failedAt) {
        requiresNew.executeWithoutResult(status -> jdbcTemplate.update("""
                update reliable_message_processing
                set status = 'FAILED', lease_until = null, last_error = ?, updated_at = ?
                where consumer_scope = ? and fingerprint = ? and status = 'PROCESSING'
                """, error, timestamp(failedAt), consumerScope, fingerprint));
    }

    private static Timestamp timestamp(Instant instant) {
        return Timestamp.from(instant);
    }
}
