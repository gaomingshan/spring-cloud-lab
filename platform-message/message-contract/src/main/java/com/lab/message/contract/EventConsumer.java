package com.lab.message.contract;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Consumer binding on an {@link EventHandler} implementation type (not on arbitrary methods).
 * One handler type maps to one topic + group. Independent of {@link EventProducer} on the event class.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventConsumer {
    String topic();

    String group();
}
