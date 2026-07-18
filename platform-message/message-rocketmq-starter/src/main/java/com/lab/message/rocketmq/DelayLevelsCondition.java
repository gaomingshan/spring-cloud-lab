package com.lab.message.rocketmq;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;

import java.util.Map;

public final class DelayLevelsCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return Binder.get(context.getEnvironment())
                .bind("lab.message.rocketmq.delay-levels", Bindable.mapOf(String.class, Integer.class))
                .map(Map::isEmpty)
                .orElse(true) == false;
    }
}
