package com.lab.message.kafka.adapter.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventHandler;
import com.lab.message.contract.EventSubscriber;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

public final class KafkaEventSubscriber implements EventSubscriber, DisposableBean {
    private final ObjectMapper objectMapper;
    private final KafkaHandlerDescriptorResolver descriptorResolver;
    private final KafkaListenerRegistrar listenerRegistrar;

    public KafkaEventSubscriber(
            ConcurrentKafkaListenerContainerFactory<String, String> containerFactory,
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.descriptorResolver = new KafkaHandlerDescriptorResolver();
        this.listenerRegistrar = new KafkaListenerRegistrar(containerFactory);
    }

    @Override
    public <E extends BaseEvent> void bind(EventHandler<E> handler) {
        KafkaHandlerDescriptor<E> descriptor = descriptorResolver.resolve(handler);
        listenerRegistrar.register(
                descriptor, new KafkaTypedListener<>(descriptor.eventType(), descriptor.handler(), objectMapper));
    }

    @Override
    public void destroy() {
        listenerRegistrar.destroy();
    }
}
