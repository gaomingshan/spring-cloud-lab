package com.lab.message.rocketmq.adapter.subscriber;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventHandler;
import com.lab.message.contract.MessageException;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.converter.MessageConverter;

import java.nio.charset.StandardCharsets;

/**
 * RocketMQ transport listener with a fixed {@link MessageExt} signature.
 * The adapter converts the message body to the concrete event type before invoking the handler.
 */
@RequiredArgsConstructor
final class RocketMqTypedListener<E extends BaseEvent> implements RocketMQListener<MessageExt> {
    private final Class<E> eventType;
    private final EventHandler<E> handler;
    private final MessageConverter messageConverter;

    @Override
    public void onMessage(MessageExt message) {
        E event = convert(message);
        handler.handle(event);
    }

    private E convert(MessageExt message) {
        if (message == null || message.getBody() == null) {
            throw new MessageException("DESERIALIZE_FAILED: empty RocketMQ message for " + eventType.getName());
        }
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        Object converted = messageConverter.fromMessage(
                MessageBuilder.withPayload(body).copyHeaders(message.getProperties()).build(),
                eventType);
        if (!eventType.isInstance(converted)) {
            throw new MessageException("DESERIALIZE_FAILED: expected " + eventType.getName()
                    + " but got " + (converted == null ? "null" : converted.getClass().getName()));
        }
        return eventType.cast(converted);
    }
}
