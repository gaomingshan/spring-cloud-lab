package com.lab.message.kafka.adapter.subscriber;

import com.lab.message.contract.EventConsumer;
import com.lab.message.contract.EventHandler;
import com.lab.message.contract.EventSubscriber;
import com.lab.message.contract.MessageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;

@Slf4j
@RequiredArgsConstructor
public final class KafkaConsumerRegistrar implements SmartInitializingSingleton {
    private final ApplicationContext applicationContext;
    private final ObjectProvider<EventSubscriber> eventSubscriberProvider;

    @Override
    public void afterSingletonsInstantiated() {
        for (String beanName : applicationContext.getBeanNamesForType(EventHandler.class)) {
            EventHandler<?> handler = applicationContext.getBean(beanName, EventHandler.class);
            Class<?> handlerType = ClassUtils.getUserClass(handler);
            if (!AnnotatedElementUtils.hasAnnotation(handlerType, EventConsumer.class)) {
                continue;
            }
            try {
                eventSubscriberProvider.getObject().bind(handler);
                log.info("Auto-registered Kafka EventHandler {}", handlerType.getName());
            } catch (MessageException e) {
                throw e;
            } catch (Exception e) {
                throw new MessageException("CONFIGURATION_FAILED: failed to register " + handlerType.getName(), e);
            }
        }
    }
}
