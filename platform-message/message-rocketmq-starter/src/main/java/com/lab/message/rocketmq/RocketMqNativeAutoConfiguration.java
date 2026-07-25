package com.lab.message.rocketmq;

import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * Native RocketMQ layer only — personalizes official {@link RocketMQProperties} / {@code rocketmq.*}.
 * <p>
 * Does <strong>not</strong> register message-contract facade beans; those live in
 * {@code message-rocketmq-adapter} auto-configuration.
 */
@AutoConfiguration
@AutoConfigureBefore(name = "org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration")
@EnableConfigurationProperties(RocketMqNativeProperties.class)
@ConditionalOnClass(RocketMQProperties.class)
@ConditionalOnProperty(prefix = "lab.message.rocketmq", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RocketMqNativeAutoConfiguration {

    /**
     * Fills blank official {@code rocketmq.*} fields from lab native defaults (Feign-style overlay).
     */
    @Bean
    static BeanPostProcessor rocketMqNativePropertiesCustomizer(RocketMqNativeProperties lab,
                                                                Environment environment) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (!(bean instanceof RocketMQProperties props)) {
                    return bean;
                }
                applyNativeDefaults(props, lab, environment);
                return bean;
            }
        };
    }

    static void applyNativeDefaults(RocketMQProperties props,
                                    RocketMqNativeProperties lab,
                                    Environment environment) {
        if (!StringUtils.hasText(props.getNameServer()) && StringUtils.hasText(lab.getNameServer())) {
            props.setNameServer(lab.getNameServer());
        }
        if (!StringUtils.hasText(props.getAccessChannel()) && StringUtils.hasText(lab.getAccessChannel())) {
            props.setAccessChannel(lab.getAccessChannel());
        }

        RocketMQProperties.Producer producer = props.getProducer();
        if (producer == null) {
            producer = new RocketMQProperties.Producer();
            props.setProducer(producer);
        }
        RocketMqNativeProperties.Producer labProducer = lab.getProducer();
        if (!StringUtils.hasText(producer.getGroup())) {
            String group = labProducer.getGroup();
            if (!StringUtils.hasText(group)) {
                String app = environment.getProperty("spring.application.name", "application");
                group = app + "-producer";
            }
            producer.setGroup(group);
        }
        if (labProducer.getSendMessageTimeout() != null) {
            // only overlay when still at library default 3000 and lab differs — always set from lab if official not customized is hard;
            // apply lab timeout as platform default when property rocketmq.producer.send-message-timeout absent
            if (!environment.containsProperty("rocketmq.producer.send-message-timeout")) {
                producer.setSendMessageTimeout((int) labProducer.getSendMessageTimeout().toMillis());
            }
        }
        if (!environment.containsProperty("rocketmq.producer.retry-times-when-send-failed")) {
            producer.setRetryTimesWhenSendFailed(labProducer.getRetryTimesWhenSendFailed());
        }
        if (!environment.containsProperty("rocketmq.producer.retry-times-when-send-async-failed")) {
            producer.setRetryTimesWhenSendAsyncFailed(labProducer.getRetryTimesWhenSendAsyncFailed());
        }
        if (!environment.containsProperty("rocketmq.producer.retry-next-server")) {
            producer.setRetryNextServer(labProducer.isRetryNextServer());
        }
        if (!environment.containsProperty("rocketmq.producer.max-message-size")) {
            producer.setMaxMessageSize(labProducer.getMaxMessageSize());
        }
        if (!environment.containsProperty("rocketmq.producer.enable-msg-trace")) {
            producer.setEnableMsgTrace(labProducer.isEnableMsgTrace());
        }
        if (!environment.containsProperty("rocketmq.producer.tls-enable")) {
            producer.setTlsEnable(labProducer.isTlsEnable());
        }

        RocketMQProperties.PushConsumer consumer = props.getConsumer();
        if (consumer == null) {
            return;
        }
        RocketMqNativeProperties.Consumer labConsumer = lab.getConsumer();
        if (!environment.containsProperty("rocketmq.consumer.message-model")
                && StringUtils.hasText(labConsumer.getMessageModel())) {
            consumer.setMessageModel(labConsumer.getMessageModel());
        }
        if (!environment.containsProperty("rocketmq.consumer.selector-type")
                && StringUtils.hasText(labConsumer.getSelectorType())) {
            consumer.setSelectorType(labConsumer.getSelectorType());
        }
    }
}
