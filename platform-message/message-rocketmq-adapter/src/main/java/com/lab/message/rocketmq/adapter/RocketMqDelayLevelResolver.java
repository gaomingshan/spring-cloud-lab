package com.lab.message.rocketmq.adapter;

import com.lab.message.contract.MessageException;
import lombok.Getter;

import java.time.Duration;
import java.util.Map;

@Getter
public final class RocketMqDelayLevelResolver {
    private final Map<Duration, Integer> delayLevels;

    public RocketMqDelayLevelResolver(Map<Duration, Integer> delayLevels) {
        this.delayLevels = delayLevels == null || delayLevels.isEmpty()
                ? Map.of()
                : Map.copyOf(delayLevels);
    }

    public boolean isAvailable() {
        return !delayLevels.isEmpty();
    }

    public int resolve(Duration delay) {
        if (delay == null) {
            throw new MessageException("CAPABILITY_UNAVAILABLE: RocketMQ delay is null");
        }
        Integer level = delayLevels.get(delay);
        if (level == null) {
            throw new MessageException("CAPABILITY_UNAVAILABLE: RocketMQ delay level is not configured for " + delay);
        }
        return level;
    }
}
