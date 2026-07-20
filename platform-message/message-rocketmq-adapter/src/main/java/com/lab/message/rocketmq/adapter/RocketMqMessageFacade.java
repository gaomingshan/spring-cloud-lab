package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.ConsumptionMode;
import com.lab.message.contract.DelayedEventPublisher;
import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.EventHandler;
import com.lab.message.contract.EventPublisher;
import com.lab.message.contract.EventSubscriber;
import com.lab.message.contract.EventSubscription;
import com.lab.message.contract.MessageException;
import com.lab.message.contract.OrderedEventPublisher;
import com.lab.message.contract.TransactionalEventPublisher;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQMessageListenerContainerRegistrar;
import org.apache.rocketmq.spring.support.RocketMQMessageConverter;
import org.springframework.messaging.Message;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.Map;

public final class RocketMqMessageFacade implements EventPublisher, OrderedEventPublisher,
        DelayedEventPublisher, TransactionalEventPublisher, EventSubscriber {
    private final RocketMQTemplate template;
    private final RocketMqMessageMapper mapper;
    private final Map<Duration, Integer> delayLevels;
    private final RocketMQMessageListenerContainerRegistrar registrar;
    private final RocketMQProperties properties;

    public RocketMqMessageFacade(RocketMQTemplate template, RocketMqMessageMapper mapper,
                                 Map<Duration, Integer> delayLevels,
                                 RocketMQMessageListenerContainerRegistrar registrar,
                                 RocketMQProperties properties) {
        if (template == null || mapper == null || registrar == null || properties == null) {
            throw new MessageException("CONFIGURATION_FAILED: RocketMQ facade dependencies are required");
        }
        this.template = template;
        this.mapper = mapper;
        this.delayLevels = delayLevels == null ? Map.of() : Map.copyOf(delayLevels);
        this.registrar = registrar;
        this.properties = properties;
    }

    @Override
    public void publish(EventEnvelope<?> event) {
        send(template.syncSend(mapper.topic(event), mapper.map(event)), "ordinary");
    }

    @Override
    public void publishOrdered(EventEnvelope<?> event) {
        requirePartitionKey(event);
        send(template.syncSendOrderly(mapper.topic(event), mapper.map(event), event.partitionKey()), "ordered");
    }

    @Override
    public void publishDelayed(EventEnvelope<?> event, Duration delay) {
        Integer delayLevel = delay == null ? null : delayLevels.get(delay);
        if (delayLevel == null) {
            throw new MessageException("CAPABILITY_UNAVAILABLE: RocketMQ delay level is not configured");
        }
        send(template.syncSend(mapper.topic(event), mapper.map(event),
                template.getProducer().getSendMsgTimeout(), delayLevel), "delayed");
    }

    @Override
    public void publishInTransaction(EventEnvelope<?> event) {
        try {
            template.sendMessageInTransaction(mapper.topic(event), mapper.map(event), event);
        } catch (Exception e) {
            throw new MessageException("ROCKETMQ_TRANSACTION_FAILED: transaction send failed", e);
        }
    }

    @Override
    public void subscribe(EventSubscription subscription, EventHandler handler) {
        if (subscription == null || handler == null) {
            throw new MessageException("VALIDATION_FAILED: subscription and handler are required");
        }
        registrar.registerContainer(
                "rocketMqFacadeListener-" + subscription.consumerGroup() + "-" + subscription.destination(),
                new RocketMqEnvelopeListener(handler),
                listenerConfiguration(subscription));
    }

    private RocketMQMessageListener listenerConfiguration(EventSubscription subscription) {
        ConsumeMode consumeMode = subscription.consumptionMode() == ConsumptionMode.ORDERED
                ? ConsumeMode.ORDERLY : ConsumeMode.CONCURRENTLY;
        InvocationHandler handler = new ListenerConfiguration(subscription, consumeMode, properties);
        return (RocketMQMessageListener) Proxy.newProxyInstance(
                RocketMQMessageListener.class.getClassLoader(),
                new Class<?>[]{RocketMQMessageListener.class}, handler);
    }

    private static void requirePartitionKey(EventEnvelope<?> event) {
        if (event == null || event.partitionKey() == null || event.partitionKey().isBlank()) {
            throw new MessageException("VALIDATION_FAILED: partitionKey is required for ordered publishing");
        }
    }

    private static void send(org.apache.rocketmq.client.producer.SendResult result, String mode) {
        if (result == null || result.getSendStatus() != org.apache.rocketmq.client.producer.SendStatus.SEND_OK) {
            throw new MessageException("ROCKETMQ_" + mode.toUpperCase() + "_SEND_FAILED");
        }
    }

    private static final class RocketMqEnvelopeListener implements RocketMQListener<EventEnvelope<?>> {
        private final EventHandler handler;

        private RocketMqEnvelopeListener(EventHandler handler) { this.handler = handler; }

        @Override
        public void onMessage(EventEnvelope<?> event) { handler.handle(event); }
    }

    private record ListenerConfiguration(EventSubscription subscription, ConsumeMode consumeMode,
                                         RocketMQProperties properties) implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "consumerGroup" -> subscription.consumerGroup();
                case "topic" -> subscription.destination();
                case "selectorType" -> selectorType();
                case "selectorExpression" -> subscription.selector();
                case "consumeMode" -> consumeMode;
                case "messageModel" -> messageModel();
                case "annotationType" -> RocketMQMessageListener.class;
                case "accessKey" -> RocketMQMessageListener.ACCESS_KEY_PLACEHOLDER;
                case "secretKey" -> RocketMQMessageListener.SECRET_KEY_PLACEHOLDER;
                case "customizedTraceTopic" -> RocketMQMessageListener.TRACE_TOPIC_PLACEHOLDER;
                case "nameServer" -> RocketMQMessageListener.NAME_SERVER_PLACEHOLDER;
                case "accessChannel" -> RocketMQMessageListener.ACCESS_CHANNEL_PLACEHOLDER;
                default -> method.getDefaultValue();
            };
        }

        private SelectorType selectorType() {
            return SelectorType.valueOf(properties.getConsumer().getSelectorType().toUpperCase(java.util.Locale.ROOT));
        }

        private MessageModel messageModel() {
            return MessageModel.valueOf(properties.getConsumer().getMessageModel().toUpperCase(java.util.Locale.ROOT));
        }
    }
}
