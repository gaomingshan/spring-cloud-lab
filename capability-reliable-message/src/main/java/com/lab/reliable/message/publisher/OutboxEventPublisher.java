package com.lab.reliable.message.publisher;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventPublisher;
import com.lab.message.contract.MessageException;
import com.lab.reliable.message.codec.EventCodec;
import com.lab.reliable.message.codec.SerializedEvent;
import com.lab.reliable.message.model.DeliveryMode;
import com.lab.reliable.message.store.ReliableMessageStore;
import java.time.Clock;
import java.util.UUID;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public final class OutboxEventPublisher implements EventPublisher {
    private final ReliableMessageStore store;
    private final EventCodec codec;
    private final Clock clock;

    public OutboxEventPublisher(ReliableMessageStore store, EventCodec codec, Clock clock) {
        this.store = store;
        this.codec = codec;
        this.clock = clock;
    }

    @Override
    public void publish(BaseEvent event) {
        append(event, DeliveryMode.ORDINARY);
    }

    @Override
    public void publishOrdered(BaseEvent event) {
        append(event, DeliveryMode.ORDERED);
    }

    private void append(BaseEvent event, DeliveryMode deliveryMode) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new MessageException("TRANSACTION_REQUIRED: reliable event publishing requires an active transaction");
        }
        if (event == null) {
            throw new MessageException("VALIDATION_FAILED: event is required");
        }
        SerializedEvent serialized = codec.encode(event);
        store.appendOutbox(UUID.randomUUID().toString(), serialized.eventType(), serialized.payload(), deliveryMode,
                clock.instant());
    }
}
