package com.lab.reliable.message.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab.message.contract.EventPublisher;
import com.lab.message.contract.EventSubscriber;
import com.lab.reliable.message.codec.EventCodec;
import com.lab.reliable.message.codec.JacksonEventCodec;
import com.lab.reliable.message.publisher.OutboxDispatcher;
import com.lab.reliable.message.publisher.OutboxEventPublisher;
import com.lab.reliable.message.publisher.OutboxDispatchScheduler;
import com.lab.reliable.message.store.JdbcReliableMessageStore;
import com.lab.reliable.message.store.ReliableMessageStore;
import com.lab.reliable.message.subscriber.ExactlyOnceEventSubscriber;
import java.time.Clock;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
@AutoConfiguration(afterName = {
        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
        "org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration",
        "com.lab.message.kafka.adapter.config.KafkaAdapterAutoConfiguration",
        "com.lab.message.rocketmq.adapter.config.RocketMqAdapterAutoConfiguration"
})
@EnableScheduling
@EnableConfigurationProperties(ReliableMessageProperties.class)
@ConditionalOnProperty(prefix = "reliable.message", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(value = {DataSource.class, PlatformTransactionManager.class, ObjectMapper.class},
        name = {"messageEventPublisher", "messageEventSubscriber"})
public class ReliableMessageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    Clock reliableMessageClock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnMissingBean
    JdbcTemplate reliableMessageJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @ConditionalOnMissingBean
    ReliableMessageStore reliableMessageStore(JdbcTemplate reliableMessageJdbcTemplate,
                                              PlatformTransactionManager transactionManager) {
        return new JdbcReliableMessageStore(reliableMessageJdbcTemplate, transactionManager);
    }

    @Bean
    @ConditionalOnMissingBean
    EventCodec reliableMessageEventCodec(ObjectMapper objectMapper) {
        return new JacksonEventCodec(objectMapper);
    }

    @Bean
    @Primary
    EventPublisher eventPublisher(ReliableMessageStore reliableMessageStore, EventCodec reliableMessageEventCodec,
                                  Clock reliableMessageClock) {
        return new OutboxEventPublisher(reliableMessageStore, reliableMessageEventCodec, reliableMessageClock);
    }

    @Bean
    @Primary
    EventSubscriber eventSubscriber(
            @Qualifier("messageEventSubscriber") EventSubscriber delegate,
            ReliableMessageStore reliableMessageStore,
            ReliableMessageProperties properties,
            Clock reliableMessageClock,
            PlatformTransactionManager transactionManager) {
        return new ExactlyOnceEventSubscriber(delegate, reliableMessageStore, properties, reliableMessageClock,
                transactionManager);
    }

    @Bean
    OutboxDispatcher outboxDispatcher(
            @Qualifier("messageEventPublisher") EventPublisher publisher,
            ReliableMessageStore reliableMessageStore,
            EventCodec reliableMessageEventCodec,
            ReliableMessageProperties properties,
            Clock reliableMessageClock) {
        return new OutboxDispatcher(publisher, reliableMessageStore, reliableMessageEventCodec, properties,
                reliableMessageClock);
    }

    @Bean
    OutboxDispatchScheduler outboxDispatchScheduler(OutboxDispatcher outboxDispatcher) {
        return new OutboxDispatchScheduler(outboxDispatcher);
    }
}
