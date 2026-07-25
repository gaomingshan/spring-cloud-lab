package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventConsumer;
import com.lab.message.contract.EventHandler;
import com.lab.message.contract.EventSubscriber;
import com.lab.message.contract.MessageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

/**
 * Discovers {@link EventConsumer} methods and registers them with the RocketMQ facade subscriber.
 */
@Slf4j
@RequiredArgsConstructor
public final class RocketMqConsumerRegistrar implements SmartInitializingSingleton {
    private final ApplicationContext applicationContext;
    private final EventSubscriber eventSubscriber;

    @Override
    public void afterSingletonsInstantiated() {
        String[] names = applicationContext.getBeanDefinitionNames();
        for (String name : names) {
            Object bean;
            try {
                bean = applicationContext.getBean(name);
            } catch (Exception ex) {
                continue;
            }
            Class<?> userClass = ClassUtils.getUserClass(bean);
            if (userClass.getName().startsWith("org.springframework.")) {
                continue;
            }
            Map<Method, EventConsumer> methods = MethodIntrospector.selectMethods(userClass,
                    (MethodIntrospector.MetadataLookup<EventConsumer>) method ->
                            AnnotatedElementUtils.findMergedAnnotation(method, EventConsumer.class));
            for (Map.Entry<Method, EventConsumer> entry : methods.entrySet()) {
                registerMethod(bean, entry.getKey(), entry.getValue());
            }
            // class-level @EventConsumer + EventHandler
            EventConsumer typeConsumer = AnnotatedElementUtils.findMergedAnnotation(userClass, EventConsumer.class);
            if (typeConsumer != null && bean instanceof EventHandler<?> handler) {
                registerHandlerBean(handler, typeConsumer);
            }
        }
    }

    private void registerMethod(Object bean, Method method, EventConsumer consumer) {
        Method target = AopUtils.selectInvocableMethod(method, bean.getClass());
        Class<?>[] params = target.getParameterTypes();
        if (params.length != 1 || !BaseEvent.class.isAssignableFrom(params[0])) {
            throw new MessageException("VALIDATION_FAILED: @EventConsumer method must have a single BaseEvent parameter: "
                    + userClassName(bean) + "#" + target.getName());
        }
        Class<? extends BaseEvent> eventType = params[0].asSubclass(BaseEvent.class);
        if (eventType == BaseEvent.class || Modifier.isAbstract(eventType.getModifiers())) {
            throw new MessageException("VALIDATION_FAILED: @EventConsumer parameter must be a concrete event type: "
                    + eventType.getName());
        }
        EventHandler<? extends BaseEvent> handler = event -> {
            try {
                ReflectionUtils.makeAccessible(target);
                target.invoke(bean, event);
            } catch (MessageException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new MessageException("CONSUMER_FAILED: " + userClassName(bean) + "#" + target.getName(), ex);
            }
        };
        bindUnchecked(eventType, consumer.topic(), consumer.group(), handler);
        log.info("Registered @EventConsumer method {}#{} topic={} group={} eventType={}",
                userClassName(bean), target.getName(), consumer.topic(), consumer.group(), eventType.getName());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void registerHandlerBean(EventHandler handler, EventConsumer consumer) {
        Class<?> eventType = resolveHandlerEventType(handler.getClass());
        if (eventType == null || eventType == BaseEvent.class || Modifier.isAbstract(eventType.getModifiers())) {
            throw new MessageException("VALIDATION_FAILED: EventHandler bean with @EventConsumer needs concrete type arg: "
                    + handler.getClass().getName());
        }
        bindUnchecked((Class) eventType, consumer.topic(), consumer.group(), handler);
        log.info("Registered @EventConsumer handler {} topic={} group={} eventType={}",
                handler.getClass().getName(), consumer.topic(), consumer.group(), eventType.getName());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void bindUnchecked(Class eventType, String topic, String group, EventHandler handler) {
        eventSubscriber.bind(eventType, topic, group, handler);
    }

    private static Class<?> resolveHandlerEventType(Class<?> handlerClass) {
        return org.springframework.core.ResolvableType.forClass(handlerClass)
                .as(EventHandler.class)
                .getGeneric(0)
                .resolve();
    }

    private static String userClassName(Object bean) {
        return ClassUtils.getUserClass(bean).getName();
    }
}
