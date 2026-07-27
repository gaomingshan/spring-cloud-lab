package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.BaseEvent;
import com.lab.message.contract.EventHandler;
import com.lab.message.contract.EventSubscriber;
import com.lab.message.rocketmq.adapter.config.RocketMqAdapterProperties;
import org.apache.rocketmq.spring.support.RocketMQMessageListenerContainerRegistrar;

public final class RocketMqEventSubscriber implements EventSubscriber {

    private final RocketMqHandlerDescriptorResolver descriptorResolver;
    private final RocketMqListenerDefinitionFactory definitionFactory;
    private final RocketMqListenerRegistrar listenerRegistrar;

    private RocketMqEventSubscriber(
        RocketMqHandlerDescriptorResolver descriptorResolver,
        RocketMqListenerDefinitionFactory definitionFactory,
        RocketMqListenerRegistrar listenerRegistrar
    ) {
        this.descriptorResolver = descriptorResolver;
        this.definitionFactory = definitionFactory;
        this.listenerRegistrar = listenerRegistrar;
    }

    public static RocketMqEventSubscriber create(
        RocketMQMessageListenerContainerRegistrar registrar,
        RocketMqAdapterProperties adapterProperties
    ) {
        return new RocketMqEventSubscriber(
            new RocketMqHandlerDescriptorResolver(),
            new RocketMqListenerDefinitionFactory(adapterProperties),
            new RocketMqListenerRegistrar(registrar));
    }

    @Override
    public <E extends BaseEvent> void bind(EventHandler<E> handler) {
        RocketMqHandlerDescriptor<E> descriptor = descriptorResolver.resolve(handler);
        RocketMqListenerDefinition<E> definition = definitionFactory.create(descriptor);
        listenerRegistrar.register(definition);
    }
}
