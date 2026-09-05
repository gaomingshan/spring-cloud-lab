package com.lab.message.kafka.adapter.subscriber;

import com.lab.message.contract.BaseEvent;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

final class KafkaListenerRegistrar implements DisposableBean {
    private final ConcurrentKafkaListenerContainerFactory<String, String> containerFactory;
    private final List<ConcurrentMessageListenerContainer<String, String>> containers = new CopyOnWriteArrayList<>();

    KafkaListenerRegistrar(ConcurrentKafkaListenerContainerFactory<String, String> containerFactory) {
        this.containerFactory = containerFactory;
    }

    <E extends BaseEvent> void register(KafkaHandlerDescriptor<E> descriptor, KafkaTypedListener<E> listener) {
        ConcurrentMessageListenerContainer<String, String> container =
                containerFactory.createContainer(descriptor.topic());
        container.getContainerProperties().setGroupId(descriptor.group());
        container.getContainerProperties().setMessageListener(listener);
        try {
            container.start();
            containers.add(container);
        } catch (RuntimeException e) {
            container.stop();
            throw e;
        }
    }

    @Override
    public void destroy() {
        containers.forEach(ConcurrentMessageListenerContainer::stop);
        containers.clear();
    }
}
