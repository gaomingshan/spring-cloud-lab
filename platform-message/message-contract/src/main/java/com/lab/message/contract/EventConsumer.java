package com.lab.message.contract;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Consumer-side binding. Declared on the consumer function (method) or handler type.
 * Independent of {@link EventProducer} on the event class.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventConsumer {
    String topic();

    String group();
}
