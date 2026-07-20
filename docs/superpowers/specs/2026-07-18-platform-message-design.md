# Platform Message Design

## Goal

Build a reusable messaging foundation that solves shared protocol, publishing, serialization, context propagation, naming, and broker-adapter problems without mixing in reliable-message business capabilities.

## Scope

The first implementation supports:

- A broker-neutral event contract.
- Shared JSON serialization without core-level payload version enforcement.
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

The event type is a stable protocol name, never a Java fully qualified class name. The payload is opaque JSON-compatible data. If an application needs payload versioning, it owns the version marker and upgrade policy in its payload or application headers; the messaging core does not interpret or enforce it.

### Publishing

```java
public interface EventPublisher {
    void publish(EventEnvelope<?> event);
}
```

Publish success is represented by normal return. Validation and transport failures throw `MessageException`. Destination, timeout, key, and headers are controlled by the envelope, naming strategy, and transport configuration rather than per-call options.

### Explicit Capabilities

Broker-specific capabilities remain explicit:

```java
public interface OrderedEventPublisher extends EventPublisher {
    void publishOrdered(EventEnvelope<?> event);
}

public interface DelayedEventPublisher extends EventPublisher {
    void publishDelayed(EventEnvelope<?> event, Duration delay);
}

public interface TransactionalEventPublisher extends EventPublisher {
    void publishInTransaction(EventEnvelope<?> event);
}
```

## Message Core

`message-contract` is the broker-neutral layer. Callers create complete `EventEnvelope` instances themselves. Each adapter owns serialization, native destination naming, mapping, and transport-specific validation while delegating execution to the underlying messaging ecosystem.

## Local Message Starter

`message-local-starter` provides a process-local publishing bridge behind the same `EventPublisher` contract. Publication delegates to Spring `ApplicationEventPublisher`, and applications receive `LocalMessageEvent` through Spring `@EventListener`.

It does not implement a second handler registry or executor. Synchronous behavior is provided by Spring's default application-event infrastructure; applications opt into asynchronous listeners through Spring's `@Async` support.

Local delivery explicitly does not promise persistence, cross-process delivery, crash recovery, consumer-group coordination, broker retry, or multi-instance broadcast. Durable local messaging belongs to the reliable-message capability layer.

## RocketMQ Adapter and Starter

`message-rocketmq-adapter` is a thin bridge over RocketMQ Spring. It uses `RocketMQTemplate`, `RocketMQMessageConverter`, `@RocketMQMessageListener`, and the official listener containers. The adapter maps `EventEnvelope` to Spring Messaging messages and exposes the common publisher capabilities without owning producer, consumer, codec, thread-pool, retry, or consume-model implementations.

`message-rocketmq-starter` only gates the adapter facade with `lab.message.rocketmq.enabled`. Producer, consumer, converter, listener, thread, retry, and consume-model configuration remains under the official `rocketmq.*` properties and annotations. User-provided `RocketMQTemplate`, `RocketMQMessageConverter`, destination resolver, and listener beans take precedence through Spring's conditional bean model.

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
    -> local starter
    -> RocketMQ adapter/starter
    -> future Stream starter
```

## Lab

`message-lab` demonstrates local synchronous/asynchronous events, RocketMQ ordinary/ordered/delayed/transactional publication, envelope headers, trace context, handler failures, and publish results. It does not implement Outbox, Inbox, consumer idempotency, or business workflows.

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
