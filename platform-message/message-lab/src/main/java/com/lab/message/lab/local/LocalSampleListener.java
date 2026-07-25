package com.lab.message.lab.local;

import com.lab.message.lab.event.OrderLifecycleEvent;
import com.lab.message.local.LocalMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "lab.message.local", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LocalSampleListener {

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
