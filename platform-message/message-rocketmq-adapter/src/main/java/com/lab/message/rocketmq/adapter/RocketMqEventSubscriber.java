package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.ConsumptionMode;
import com.lab.message.contract.EventHandler;
import com.lab.message.contract.EventSubscriber;
import com.lab.message.contract.EventSubscription;
import com.lab.message.contract.MessageException;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.support.RocketMQMessageListenerContainerRegistrar;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.function.Function;

public final class RocketMqEventSubscriber implements EventSubscriber {
    private final RocketMQMessageListenerContainerRegistrar registrar;
    private final RocketMQProperties properties;
    private final Function<String, EventSubscription> bindingLookup;

    public RocketMqEventSubscriber(RocketMQMessageListenerContainerRegistrar registrar,
                                   RocketMQProperties properties,
                                   Function<String, EventSubscription> bindingLookup) {
        if (registrar == null || properties == null || bindingLookup == null) {
            throw new MessageException("CONFIGURATION_FAILED: RocketMQ subscriber dependencies are required");
        }
        this.registrar = registrar;
        this.properties = properties;
        this.bindingLookup = bindingLookup;
    }

    public static Function<String, EventSubscription> lookupFromMap(Map<String, EventSubscription> bindings) {
        Map<String, EventSubscription> copy = bindings == null ? Map.of() : Map.copyOf(bindings);
        return name -> {
            EventSubscription subscription = copy.get(name);
            if (subscription == null) {
                throw new MessageException("CONFIGURATION_FAILED: consumer binding not found: " + name);
            }
            return subscription;
        };
    }

    @Override
    public <E extends BaseEvent> void bind(String bindingName, Class<E> eventType, EventHandler<E> handler) {
        if (bindingName == null || bindingName.isBlank()) {
            throw new MessageException("VALIDATION_FAILED: bindingName is required");
        }
        if (eventType == null || handler == null) {
            throw new MessageException("VALIDATION_FAILED: eventType and handler are required");
        }
        if (eventType == BaseEvent.class || Modifier.isAbstract(eventType.getModifiers()) || eventType.isInterface()) {
            throw new MessageException("VALIDATION_FAILED: bind requires a concrete event type, not " + eventType.getName());
        }
        EventSubscription subscription = bindingLookup.apply(bindingName.trim());
        String beanName = "labRocketMqListener-" + bindingName + "-" + eventType.getSimpleName();
        RocketMQMessageListener annotation = RocketMqListenerAnnotation.from(subscription, properties);
        registrar.registerContainer(beanName, new TypedListener<>(eventType, handler), annotation);
    }

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
