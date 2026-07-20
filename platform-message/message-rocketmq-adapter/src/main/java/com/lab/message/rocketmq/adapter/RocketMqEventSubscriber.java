package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.ConsumptionMode;
import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.EventHandler;
import com.lab.message.contract.EventSubscriber;
import com.lab.message.contract.EventSubscription;
import com.lab.message.contract.MessageException;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.apache.rocketmq.spring.support.RocketMQMessageConverter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public final class RocketMqEventSubscriber implements EventSubscriber, AutoCloseable {
    private final RocketMQProperties properties;
    private final RocketMQMessageConverter messageConverter;
    private final ApplicationContext applicationContext;
    private final List<DefaultRocketMQListenerContainer> containers = new ArrayList<>();
    private final Map<DefaultRocketMQListenerContainer, String> containerNames = new IdentityHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public RocketMqEventSubscriber(RocketMQProperties properties, RocketMQMessageConverter messageConverter,
                                   ApplicationContext applicationContext) {
        if (properties == null || messageConverter == null || applicationContext == null) {
            throw new MessageException("CONFIGURATION_FAILED: RocketMQ consumer dependencies are required");
        }
        this.properties = properties;
        this.messageConverter = messageConverter;
        this.applicationContext = applicationContext;
    }

    @Override
    public synchronized void subscribe(EventSubscription subscription, EventHandler handler) {
        if (subscription == null || handler == null) {
            throw new MessageException("VALIDATION_FAILED: subscription and handler are required");
        }
        DefaultRocketMQListenerContainer container = new DefaultRocketMQListenerContainer();
        String containerName = "rocketMqEventSubscriber-" + sequence.incrementAndGet();
        container.setName(containerName);
        container.setApplicationContext(applicationContext);
        container.setNameServer(properties.getNameServer());
        container.setConsumerGroup(subscription.consumerGroup());
        container.setTopic(subscription.destination());
        container.setSelectorExpression(subscription.selector());
        container.setMessageConverter(messageConverter.getMessageConverter());
        container.setRocketMQMessageListener(listenerConfiguration(subscription, properties));
        container.setRocketMQListener(new DelegatingListener(handler));
        ConfigurableApplicationContext configurableContext = configurableContext();
        try {
            configurableContext.getBeanFactory().registerSingleton(containerName, container);
            container.afterPropertiesSet();
            container.start();
            containers.add(container);
            containerNames.put(container, containerName);
        } catch (Exception e) {
            if (configurableContext.getBeanFactory().containsSingleton(containerName)) {
                singletonRegistry(configurableContext).destroySingleton(containerName);
            }
            container.destroy();
            throw new MessageException("ROCKETMQ_SUBSCRIBE_FAILED: could not start listener container", e);
        }
    }

    @Override
    public synchronized void close() {
        containers.forEach(container -> {
            try {
                ConfigurableApplicationContext context = configurableContext();
                String containerName = containerNames.remove(container);
                if (context.getBeanFactory().containsSingleton(containerName)) {
                    singletonRegistry(context).destroySingleton(containerName);
                } else {
                    container.destroy();
                }
            } catch (RuntimeException ignored) {
                // Continue closing all native consumers when one container fails.
            }
        });
        containers.clear();
        containerNames.clear();
    }

    private ConfigurableApplicationContext configurableContext() {
        if (!(applicationContext instanceof ConfigurableApplicationContext configurableContext)) {
            throw new MessageException("CONFIGURATION_FAILED: configurable Spring context is required");
        }
        return configurableContext;
    }

    private static DefaultSingletonBeanRegistry singletonRegistry(ConfigurableApplicationContext context) {
        return (DefaultSingletonBeanRegistry) context.getBeanFactory();
    }

    private static RocketMQMessageListener listenerConfiguration(EventSubscription subscription,
                                                                  RocketMQProperties properties) {
        ConsumeMode consumeMode = subscription.consumptionMode() == ConsumptionMode.ORDERED
                ? ConsumeMode.ORDERLY : ConsumeMode.CONCURRENTLY;
        InvocationHandler handler = new ListenerAnnotationHandler(subscription, consumeMode, properties);
        return (RocketMQMessageListener) Proxy.newProxyInstance(
                RocketMQMessageListener.class.getClassLoader(),
                new Class<?>[]{RocketMQMessageListener.class}, handler);
    }

    private final class DelegatingListener implements RocketMQListener<EventEnvelope<?>> {
        private final EventHandler handler;

        private DelegatingListener(EventHandler handler) { this.handler = handler; }

        @Override
        public void onMessage(EventEnvelope<?> event) { handler.handle(event); }
    }

    private record ListenerAnnotationHandler(EventSubscription subscription, ConsumeMode consumeMode,
                                             RocketMQProperties properties)
            implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "consumerGroup" -> subscription.consumerGroup();
                case "topic" -> subscription.destination();
                case "selectorType" -> selectorType(properties);
                case "selectorExpression" -> subscription.selector();
                case "consumeMode" -> consumeMode;
                case "messageModel" -> messageModel(properties);
                case "annotationType" -> RocketMQMessageListener.class;
                // Keep native annotation defaults; the official consumer properties
                // are applied by the official Spring-managed listener path.
                case "consumeThreadNumber", "consumeThreadMax" -> method.getDefaultValue();
                case "maxReconsumeTimes" -> method.getDefaultValue();
                case "consumeTimeout" -> method.getDefaultValue();
                case "replyTimeout", "delayLevelWhenNextConsume", "suspendCurrentQueueTimeMillis",
                     "awaitTerminationMillisWhenShutdown" -> 0;
                case "accessKey" -> RocketMQMessageListener.ACCESS_KEY_PLACEHOLDER;
                case "secretKey" -> RocketMQMessageListener.SECRET_KEY_PLACEHOLDER;
                case "customizedTraceTopic" -> RocketMQMessageListener.TRACE_TOPIC_PLACEHOLDER;
                case "nameServer" -> RocketMQMessageListener.NAME_SERVER_PLACEHOLDER;
                case "accessChannel" -> RocketMQMessageListener.ACCESS_CHANNEL_PLACEHOLDER;
                case "tlsEnable", "namespace", "namespaceV2", "instanceName" -> method.getDefaultValue();
                case "enableMsgTrace" -> method.getDefaultValue();
                default -> defaultValue(method.getReturnType());
            };
        }

        private SelectorType selectorType(RocketMQProperties properties) {
            return SelectorType.valueOf(properties.getConsumer().getSelectorType().toUpperCase(java.util.Locale.ROOT));
        }

        private MessageModel messageModel(RocketMQProperties properties) {
            return MessageModel.valueOf(properties.getConsumer().getMessageModel().toUpperCase(java.util.Locale.ROOT));
        }

        private static Object defaultValue(Class<?> type) {
            if (type == boolean.class) return false;
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == Class.class) return Object.class;
            if (type == Annotation.class) return RocketMQMessageListener.class;
            return null;
        }
    }
}
