package com.lab.message.lab.local;

import com.lab.message.local.LocalMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Local middleware sample: process-internal ApplicationEvent path.
 * Active when lab.message.local.enabled=true (profile local).
 */
@Component
@ConditionalOnProperty(prefix = "lab.message.local", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LocalSampleListener {
    private static final Logger log = LoggerFactory.getLogger(LocalSampleListener.class);

    @EventListener
    public void onLocalMessage(LocalMessageEvent event) {
        log.info("[local] received eventType={} eventId={} payload={}",
                event.envelope().eventType(),
                event.envelope().eventId(),
                event.envelope().payload());
    }
}
