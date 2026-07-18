# Platform Message Design

## Goal

Build a reusable messaging foundation that solves shared protocol, publishing, serialization, context propagation, naming, and broker-adapter problems without mixing in reliable-message business capabilities.

## Scope

The first implementation supports:

- A broker-neutral event contract.
- Shared JSON serialization and schema-version handling.
- A process-local event bus with the same public publishing contract.
- RocketMQ as the first remote broker implementation.
- Explicit RocketMQ ordered, delayed, and transactional publishing interfaces.
- A dedicated Lab for protocol and transport demonstrations.

Kafka and RabbitMQ adapters are deferred until the common contract and RocketMQ semantics are mature. Spring Cloud Stream remains a planned adapter and does not enter the common contract or core modules in the first implementation.

Transactional Outbox, Inbox, consumer idempotency, durable local messages, reliable delivery orchestration, and eventual-consistency workflows remain in `capability-reliable-message`.

## Module Structure

```text
platform-message
├── message-contract
├── message-core
├── message-local-starter
├── message-rocketmq-adapter
├── message-rocketmq-starter
└── message-lab
```

## Message Contract

`message-contract` contains no RocketMQ, Kafka, RabbitMQ, or Spring Cloud Stream types.

### Event Envelope

```java
public record EventEnvelope<T>(
        String eventId,
        String eventType,
        int schemaVersion,
        String producer,
        String aggregateType,
        String aggregateId,
        String partitionKey,
        String idempotencyKey,
        Instant occurredAt,
        String traceparent,
        Map<String, String> headers,
        T payload
) {
}
```

The event type is a stable protocol name, never a Java fully qualified class name. `schemaVersion` and `SchemaUpcaster` allow old payloads to be upgraded before handler invocation.

### Publishing

```java
public interface EventPublisher {
    PublishResult publish(EventEnvelope<?> event);
    PublishResult publish(EventEnvelope<?> event, PublishOptions options);
}
```

```java
public record PublishOptions(
        String destination,
        String key,
        Duration timeout,
        Map<String, String> headers
) {
}
```

`publish(event)` uses the configured default destination and timeout. `PublishOptions` is immutable; null maps are normalized to empty maps by the core implementation, and an explicitly provided destination or timeout overrides the configured default.

```java
public record PublishResult(
        String eventId,
        PublishStatus status,
        String messageId,
        String failureReason
) {
}
```

`SENT` means the transport accepted the send and returned a transport message ID. `ACCEPTED` is reserved for transports that enqueue work asynchronously without an immediate broker ID. `FAILED` includes validation, serialization, transport, and local-handler dispatch failures as classified causes.

The result reports publish acceptance or failure only. It never claims that a consumer has completed processing.

### Consumption and Explicit Capabilities

```java
@FunctionalInterface
public interface EventHandler<T> {
    void handle(EventEnvelope<T> event) throws Exception;
}
```

Broker-specific capabilities remain explicit:

```java
public interface OrderedEventPublisher extends EventPublisher {
    PublishResult publishOrdered(EventEnvelope<?> event, String partitionKey);
}

public interface DelayedEventPublisher extends EventPublisher {
    PublishResult publishDelayed(EventEnvelope<?> event, Duration delay);
}

public interface TransactionalEventPublisher extends EventPublisher {
    PublishResult publishTransactional(EventEnvelope<?> event);
}
```

## Message Core

`message-core` contains broker-neutral strategy and default implementations:

- Event ID, event time, producer, and header defaults.
- JSON encoder and decoder.
- Event type and schema-version validation.
- `SchemaUpcaster` and registry.
- Topic, destination, and consumer-group naming strategies.
- Request/trace context extraction and propagation.
- Publish failure classification.
- Common message exception types.

The core must not depend on a broker SDK. Its public defaults must be replaceable through interfaces or Spring beans in the Starter modules.

## Local Message Starter

`message-local-starter` provides a process-local event bus behind the same `EventPublisher` and `EventHandler` contracts.

It supports synchronous and asynchronous dispatch, handler registration, shared envelope serialization, context propagation, handler exception reporting, and executor configuration.

```yaml
lab:
  message:
    local:
      enabled: true
      dispatch-mode: async
      executor:
        core-size: 4
        max-size: 16
        queue-capacity: 1000
```

Local delivery explicitly does not promise persistence, cross-process delivery, crash recovery, consumer-group coordination, broker retry, or multi-instance broadcast. Durable local messaging belongs to the reliable-message capability layer.

## RocketMQ Adapter and Starter

`message-rocketmq-adapter` encapsulates RocketMQ SDK types and maps native messages to the common contract. It owns producer, consumer, topic/tag/key, ordered, delayed, transactional, retry, dead-letter, ACK, and message-ID behavior.

`message-rocketmq-starter` owns Spring Boot auto-configuration and exposes the default `EventPublisher` plus optional ordered, delayed, and transactional publisher beans only when enabled and supported.

```yaml
lab:
  message:
    rocketmq:
      enabled: true
      name-server: 192.168.179.128:9876
      producer:
        group: ${spring.application.name}-producer
        send-timeout: 3s
        retry-times: 2
        retry-another-broker: false
      consumer:
        group: ${spring.application.name}-consumer
        consume-thread-min: 4
        consume-thread-max: 16
        max-reconsume-times: 16
      naming:
        topic-prefix: lab
        group-prefix: lab
```

Common publishing configuration remains under the common message namespace. RocketMQ-only settings remain under `rocketmq` and do not leak into the broker-neutral API.

## Spring Cloud Stream Boundary

The future `message-stream-starter` maps the common contract to `StreamBridge`, Spring `Message`, bindings, destinations, groups, and binders. It must not redefine `EventEnvelope` or make `message-contract` depend on Stream types.

The intended graph is:

```text
message-contract
    -> message-core
        -> local starter
        -> RocketMQ adapter/starter
        -> future Stream starter
```

## Lab

`message-lab` demonstrates local synchronous/asynchronous events, RocketMQ ordinary/ordered/delayed/transactional publication, envelope headers, trace context, schema upgrades, handler failures, and publish results. It does not implement Outbox, Inbox, consumer idempotency, or business workflows.

## Starter Requirements

Every Starter must:

- Register through `AutoConfiguration.imports`.
- Use `@ConfigurationProperties` for configuration.
- Use precise class and property conditions.
- Use `@ConditionalOnMissingBean` for replaceable defaults.
- Keep broker SDKs inside adapter/starter boundaries.
- Make every exposed configuration property affect behavior.
- Preserve the common contract when adding broker-specific capabilities.
- Keep local and remote implementations behind the same public publisher interface.

## Validation

The first implementation is accepted when the full Maven reactor compiles and packages with:

```bash
mvn clean package -DskipTests
```

Runtime integration is deferred to the dedicated integration project. The Lab must still compile as an independent messaging verification application.
