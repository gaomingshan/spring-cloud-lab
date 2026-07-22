package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.EventEnvelope;
import com.lab.message.contract.EventHandler;
import com.lab.message.contract.EventSubscriber;
import com.lab.message.contract.EventSubscription;
import com.lab.message.contract.MessageException;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.support.RocketMQMessageListenerContainerRegistrar;

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
    public void subscribe(EventSubscription subscription, EventHandler handler) {
        if (subscription == null || handler == null) {
            throw new MessageException("VALIDATION_FAILED: subscription and handler are required");
        }
        String beanName = "labRocketMqListener-" + subscription.consumerGroup() + "-" + subscription.destination();
        RocketMQMessageListener annotation = RocketMqListenerAnnotation.from(subscription, properties);
        registrar.registerContainer(beanName, new EnvelopeListener(handler), annotation);
    }

    private static final class EnvelopeListener implements RocketMQListener<EventEnvelope<?>> {
        private final EventHandler handler;

        private EnvelopeListener(EventHandler handler) {
            this.handler = handler;
        }

        @Override
        public void onMessage(EventEnvelope<?> event) {
            handler.handle(event);
        }
    }
}
