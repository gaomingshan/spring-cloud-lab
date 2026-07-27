package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventConsumer;
import com.lab.message.contract.EventHandler;
import com.lab.message.contract.EventSubscriber;
import com.lab.message.contract.MessageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;

/**
 * Auto-registers only {@link EventHandler} beans that are annotated with {@link EventConsumer}. Arbitrary class methods are not
 * supported.
 */
@Slf4j
@RequiredArgsConstructor
public final class RocketMqConsumerRegistrar implements SmartInitializingSingleton {

    private final ApplicationContext applicationContext;
    private final EventSubscriber eventSubscriber;

    @Override
    public void afterSingletonsInstantiated() {
        String[] names = applicationContext.getBeanNamesForType(EventHandler.class);
        for (String name : names) {
            EventHandler<?> handler;
            try {
                handler = applicationContext.getBean(name, EventHandler.class);
            } catch (Exception ex) {
                continue;
            }
            Class<?> userClass = ClassUtils.getUserClass(handler);
            if (!AnnotatedElementUtils.hasAnnotation(userClass, EventConsumer.class)) {
                log.debug("Skip EventHandler without @EventConsumer: {}", userClass.getName());
                continue;
            }
            try {
                eventSubscriber.bind(handler);
                log.info("Auto-registered EventHandler {} with @EventConsumer", userClass.getName());
            } catch (MessageException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new MessageException("CONFIGURATION_FAILED: failed to register " + userClass.getName(), ex);
            }
        }
    }
}
