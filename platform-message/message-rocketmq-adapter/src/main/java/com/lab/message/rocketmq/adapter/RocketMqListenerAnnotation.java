package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.ConsumptionMode;
import com.lab.message.contract.EventSubscription;
import com.lab.message.contract.MessageException;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Locale;

public final class RocketMqListenerAnnotation {
    private RocketMqListenerAnnotation() {
    }

    public static RocketMQMessageListener from(EventSubscription subscription, RocketMQProperties properties) {
        if (subscription == null) {
            throw new MessageException("VALIDATION_FAILED: subscription is required");
        }
        if (properties == null) {
            throw new MessageException("CONFIGURATION_FAILED: RocketMQ properties are required");
        }
        ConsumeMode consumeMode = subscription.consumptionMode() == ConsumptionMode.ORDERED
                ? ConsumeMode.ORDERLY
                : ConsumeMode.CONCURRENTLY;
        InvocationHandler handler = new AnnotationInvocationHandler(subscription, consumeMode, properties);
        return (RocketMQMessageListener) Proxy.newProxyInstance(
                RocketMQMessageListener.class.getClassLoader(),
                new Class<?>[]{RocketMQMessageListener.class},
                handler);
    }

    private record AnnotationInvocationHandler(
            EventSubscription subscription,
            ConsumeMode consumeMode,
            RocketMQProperties properties
    ) implements InvocationHandler {
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
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                case "toString" -> "RocketMqListenerAnnotation("
                        + subscription.consumerGroup() + "@" + subscription.destination() + ")";
                default -> method.getDefaultValue();
            };
        }

        private SelectorType selectorType() {
            String raw = properties.getConsumer().getSelectorType();
            if (raw == null || raw.isBlank()) {
                return SelectorType.TAG;
            }
            return SelectorType.valueOf(raw.toUpperCase(Locale.ROOT));
        }

        private MessageModel messageModel() {
            String raw = properties.getConsumer().getMessageModel();
            if (raw == null || raw.isBlank()) {
                return MessageModel.CLUSTERING;
            }
            return MessageModel.valueOf(raw.toUpperCase(Locale.ROOT));
        }
    }
}
