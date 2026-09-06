package com.lab.reliable.message.subscriber;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventConsumer;
import com.lab.message.contract.EventHandler;
import com.lab.message.contract.EventHandlerMetadata;
import com.lab.message.contract.EventSubscriber;
import com.lab.message.contract.MessageException;
import com.lab.reliable.message.config.ReliableMessageProperties;
import com.lab.reliable.message.store.ReliableMessageStore;
import java.time.Clock;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.ClassUtils;

public final class ExactlyOnceEventSubscriber implements EventSubscriber {
    private final EventSubscriber delegate;
    private final ReliableMessageStore store;
    private final ReliableMessageProperties properties;
    private final Clock clock;
    private final PlatformTransactionManager transactionManager;

    public ExactlyOnceEventSubscriber(EventSubscriber delegate, ReliableMessageStore store,
                                     ReliableMessageProperties properties, Clock clock,
                                     PlatformTransactionManager transactionManager) {
        this.delegate = delegate;
        this.store = store;
        this.properties = properties;
        this.clock = clock;
        this.transactionManager = transactionManager;
    }

    @Override
    public <E extends BaseEvent> void bind(EventHandler<E> handler) {
        delegate.bind(new ExactlyOnceEventHandler<>(handler, consumerScope(handler), store, properties, clock,
                transactionManager));
    }

    private static String consumerScope(EventHandler<?> handler) {
        Class<?> handlerType = ClassUtils.getUserClass(EventHandlerMetadata.unwrap(handler));
        EventConsumer consumer = AnnotatedElementUtils.findMergedAnnotation(handlerType, EventConsumer.class);
        if (consumer == null || consumer.group().isBlank()) {
            throw new MessageException("VALIDATION_FAILED: @EventConsumer group is required for exactly-once processing");
        }
        return consumer.group().trim();
    }
}
