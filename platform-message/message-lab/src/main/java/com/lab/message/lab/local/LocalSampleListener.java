package com.lab.message.lab.local;

import com.lab.message.lab.event.OrderLifecycleEvent;
import com.lab.message.local.LocalMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "lab.message.local", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LocalSampleListener {
    private static final Logger log = LoggerFactory.getLogger(LocalSampleListener.class);

    @EventListener
    public void onLocalMessage(LocalMessageEvent event) {
        if (event.event() instanceof OrderLifecycleEvent orderEvent) {
            log.info("[local] phase={} eventId={} create={} payment={} cancel={}",
                    orderEvent.getPhase(),
                    orderEvent.getEventId(),
                    orderEvent.getCreate() != null ? orderEvent.getCreate().getOrderId() : null,
                    orderEvent.getPayment() != null ? orderEvent.getPayment().getPaymentId() : null,
                    orderEvent.getCancel() != null ? orderEvent.getCancel().getReason() : null);
            return;
        }
        log.info("[local] received eventClass={} eventId={}",
                event.event().getClass().getName(),
                event.event().getEventId());
    }
}
