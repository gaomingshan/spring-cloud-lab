package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventHandler;
import com.lab.message.contract.EventSubscriber;
import com.lab.message.contract.EventSubscription;
import com.lab.message.contract.MessageException;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.support.RocketMQMessageListenerContainerRegistrar;

import java.lang.reflect.Modifier;

public final class RocketMqEventSubscriber implements EventSubscriber {
    private final RocketMQMessageListenerContainerRegistrar registrar;
    private final RocketMQProperties properties;

    public RocketMqEventSubscriber(RocketMQMessageListenerContainerRegistrar registrar,
                                   RocketMQProperties properties) {
        if (registrar == null || properties == null) {
            throw new MessageException("CONFIGURATION_FAILED: RocketMQ subscriber dependencies are required");
        }
        this.registrar = registrar;
        this.properties = properties;
    }

    @Override
    public <E extends BaseEvent> void subscribe(EventSubscription subscription,
                                                Class<E> eventType,
                                                EventHandler<E> handler) {
        if (subscription == null || eventType == null || handler == null) {
            throw new MessageException("VALIDATION_FAILED: subscription, eventType and handler are required");
        }
        if (eventType == BaseEvent.class || Modifier.isAbstract(eventType.getModifiers()) || eventType.isInterface()) {
            throw new MessageException("VALIDATION_FAILED: subscribe requires a concrete event type, not " + eventType.getName());
        }
        String beanName = "labRocketMqListener-" + subscription.consumerGroup() + "-" + subscription.destination()
                + "-" + eventType.getSimpleName();
        RocketMQMessageListener annotation = RocketMqListenerAnnotation.from(subscription, properties);
        registrar.registerContainer(beanName, new TypedListener<>(eventType, handler), annotation);
    }

    /**
     * Concrete listener type so rocketmq-spring generic resolution can target {@code E}.
     * Serialization/deserialization is performed by the RocketMQ Spring converter ecosystem.
     */
    private static final class TypedListener<E extends BaseEvent> implements RocketMQListener<E> {
        private final Class<E> eventType;
        private final EventHandler<E> handler;

        private TypedListener(Class<E> eventType, EventHandler<E> handler) {
            this.eventType = eventType;
            this.handler = handler;
        }

        @Override
        public void onMessage(E event) {
            if (event == null) {
                throw new MessageException("DESERIALIZE_FAILED: null event for " + eventType.getName());
            }
            if (!eventType.isInstance(event)) {
                throw new MessageException("DESERIALIZE_FAILED: expected " + eventType.getName()
                        + " but got " + event.getClass().getName());
            }
            handler.handle(event);
        }
    }
}
