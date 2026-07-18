# Platform Message Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first reusable messaging foundation with a broker-neutral event contract, process-local event bus, RocketMQ adapter/starter, and a compilable messaging Lab.

**Architecture:** `message-contract` owns broker-neutral protocols. `message-core` owns JSON serialization, naming, validation, and context defaults. Local and RocketMQ starters implement the same `EventPublisher` contract; RocketMQ-specific ordered, delayed, and transactional APIs remain explicit. Spring Cloud Stream is deferred to a later adapter and must not enter the common contract.

**Tech Stack:** Java 21, Spring Boot 3.5.9, Spring Cloud 2025.0.0, Spring Cloud Alibaba 2025.0.0.0, Jackson, RocketMQ Java/Spring integration, Spring Boot auto-configuration.

## Global Constraints

- Java baseline remains Java 21.
- Full verification command is `mvn clean package -DskipTests`.
- Test sources are not used for integration; the root POM skips test execution.
- `platform-message` must not implement Outbox, Inbox, consumer idempotency, durable local messages, or eventual-consistency workflows.
- `message-contract` and `message-core` must not depend on RocketMQ, Kafka, RabbitMQ, or Spring Cloud Stream types.
- Kafka and RabbitMQ adapters are deferred.
- Spring Cloud Stream is deferred to a later `message-stream-starter` adapter.
- Every Starter must register with `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
- Every exposed configuration property must affect behavior.
- Existing user staged/deleted test changes must not be restored or removed.

---

### Task 1: Create Message Module Aggregation

**Files:**
- Modify: `platform-message/pom.xml`
- Create: `platform-message/message-contract/pom.xml`
- Create: `platform-message/message-core/pom.xml`
- Create: `platform-message/message-local-starter/pom.xml`
- Create: `platform-message/message-rocketmq-adapter/pom.xml`
- Create: `platform-message/message-rocketmq-starter/pom.xml`
- Create: `platform-message/message-lab/pom.xml`
- Modify: root `pom.xml` only if Maven module ordering requires it

**Interfaces:**
- Produces the Maven reactor modules consumed by Tasks 2-7.
- Keeps RocketMQ dependencies out of `message-contract` and `message-core`.

- [ ] **Step 1: Add child modules to `platform-message/pom.xml`**

```xml
<modules>
    <module>message-contract</module>
    <module>message-core</module>
    <module>message-local-starter</module>
    <module>message-rocketmq-adapter</module>
    <module>message-rocketmq-starter</module>
    <module>message-lab</module>
</modules>
```

- [ ] **Step 2: Create each child POM with the existing project parent pattern**

Every child POM uses parent `com.lab:platform-message:1.0.0-SNAPSHOT` with `../pom.xml`, sets its own artifact ID, and declares only dependencies required by that module.

- [ ] **Step 3: Run the module-only compile**

Run: `mvn -f platform-message/pom.xml -DskipTests compile`

Expected: `BUILD SUCCESS` for the aggregator and six empty child JAR modules.

- [ ] **Step 4: Commit the module skeleton**

```bash
git add platform-message
git commit -m "feat: add platform message modules"
```

### Task 2: Implement Message Contract

**Files:**
- Create: `platform-message/message-contract/src/main/java/com/lab/message/contract/EventEnvelope.java`
- Create: `platform-message/message-contract/src/main/java/com/lab/message/contract/EventPublisher.java`
- Create: `platform-message/message-contract/src/main/java/com/lab/message/contract/EventHandler.java`
- Create: `platform-message/message-contract/src/main/java/com/lab/message/contract/PublishOptions.java`
- Create: `platform-message/message-contract/src/main/java/com/lab/message/contract/PublishResult.java`
- Create: `platform-message/message-contract/src/main/java/com/lab/message/contract/PublishStatus.java`
- Create: `platform-message/message-contract/src/main/java/com/lab/message/contract/OrderedEventPublisher.java`
- Create: `platform-message/message-contract/src/main/java/com/lab/message/contract/DelayedEventPublisher.java`
- Create: `platform-message/message-contract/src/main/java/com/lab/message/contract/TransactionalEventPublisher.java`
- Create: `platform-message/message-contract/src/main/java/com/lab/message/contract/MessageException.java`

**Interfaces:**
- Produces the stable API used by local and RocketMQ implementations.
- `EventEnvelope<T>` contains `eventId`, `eventType`, `producer`, `aggregateType`, `aggregateId`, `partitionKey`, `idempotencyKey`, `occurredAt`, `traceparent`, `headers`, and `payload`.
- `PublishResult` uses `SENT`, `ACCEPTED`, and `FAILED`.

- [ ] **Step 1: Write the contract compile fixture**

Create a temporary source fixture in `message-contract/src/main/java` that constructs an envelope and invokes all publisher method signatures. Run `mvn -f platform-message/pom.xml -DskipTests compile` and verify the fixture initially fails because the types do not exist.

- [ ] **Step 2: Add immutable records and interfaces**

Use Java records for value types and defensive normalization in implementations, not in the public record constructors. Keep `EventHandler<T>` as a checked-exception functional interface.

- [ ] **Step 3: Remove the temporary fixture after the public API compiles**

The contract module must contain only production API types, not test-only fixtures.

- [ ] **Step 4: Verify the contract module**

Run: `mvn -f platform-message/message-contract/pom.xml -DskipTests compile`

Expected: `BUILD SUCCESS` with no broker SDK on the module classpath.

- [ ] **Step 5: Commit the contract**

```bash
git add platform-message/message-contract
git commit -m "feat: define message contract"
```

### Task 3: Implement Broker-Neutral Message Core

**Files:**
- Modify: `platform-message/message-core/pom.xml`
- Create: `platform-message/message-core/src/main/java/com/lab/message/core/EventEnvelopeFactory.java`
- Create: `platform-message/message-core/src/main/java/com/lab/message/core/EventSerializer.java`
- Create: `platform-message/message-core/src/main/java/com/lab/message/core/JsonEventSerializer.java`
- Create: `platform-message/message-core/src/main/java/com/lab/message/core/MessageNamingStrategy.java`
- Create: `platform-message/message-core/src/main/java/com/lab/message/core/DefaultMessageNamingStrategy.java`
- Create: `platform-message/message-core/src/main/java/com/lab/message/core/MessageCoreProperties.java`

**Interfaces:**
- `JsonEventSerializer.serialize(EventEnvelope<?>)` returns UTF-8 JSON bytes.
- `JsonEventSerializer.deserialize(byte[], Class<T>)` returns a typed envelope payload.
- `MessageNamingStrategy.destination(eventType)` and `.consumerGroup(application, purpose)` provide stable names.
- `EventEnvelopeFactory.create(eventType, payload)` supplies event ID, producer, time, and headers.

- [ ] **Step 1: Add Jackson and foundation-context dependencies**

`message-core` depends on `message-contract`, Jackson, and `foundation-context`; it must not depend on a Broker SDK.

- [ ] **Step 2: Implement the event envelope factory**

The factory reads producer from `MessageCoreProperties`, generates a UUID event ID, uses `Instant.now()`, copies current request context headers when available, and never uses a Java class name as `eventType` unless the caller explicitly supplies it.

- [ ] **Step 3: Implement JSON serialization and validation**

Reject blank `eventId`, `eventType`, producer, and null payloads with `MessageException`. Serialize stable JSON field names and preserve `headers` as a map.

- [ ] **Step 4: Implement naming defaults**

Use configurable prefixes and a normalized event type, with no Broker-specific terminology in the core API.

- [ ] **Step 6: Compile the core**

Run: `mvn -f platform-message/message-core/pom.xml -DskipTests compile`

Expected: `BUILD SUCCESS`.

- [ ] **Step 7: Commit the core**

```bash
git add platform-message/message-core
git commit -m "feat: add message core policies"
```

### Task 4: Implement Local Message Starter

**Files:**
- Modify: `platform-message/message-local-starter/pom.xml`
- Create: `platform-message/message-local-starter/src/main/java/com/lab/message/local/LocalMessageProperties.java`
- Create: `platform-message/message-local-starter/src/main/java/com/lab/message/local/LocalEventHandlerRegistry.java`
- Create: `platform-message/message-local-starter/src/main/java/com/lab/message/local/LocalEventPublisher.java`
- Create: `platform-message/message-local-starter/src/main/java/com/lab/message/local/LocalMessageAutoConfiguration.java`
- Create: `platform-message/message-local-starter/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

**Interfaces:**
- Produces `EventPublisher` for local dispatch.
- Handler registration is typed by event type and accepts `EventHandler<?>`.
- Supports `sync` and `async` dispatch modes.

- [ ] **Step 1: Define properties and executor settings**

Bind `lab.message.local.enabled`, `dispatch-mode`, `executor.core-size`, `executor.max-size`, and `executor.queue-capacity`. Validate positive sizes and reject unknown dispatch modes.

- [ ] **Step 2: Implement handler registry**

Use a concurrent map keyed by stable `eventType`. Register multiple handlers in deterministic insertion order. Handler invocation receives the same `EventEnvelope` instance and does not expose local transport types.

- [ ] **Step 3: Implement synchronous publish**

Validate the envelope, dispatch all handlers, return `SENT` with a generated local message ID on success, and return `FAILED` with a classified reason when a handler throws.

- [ ] **Step 4: Implement asynchronous publish**

Use a bounded `ThreadPoolExecutor` from the configured properties. Return `ACCEPTED` after successful queue submission. Return `FAILED` when the executor rejects submission. Preserve the event envelope and context headers; do not claim durable delivery.

- [ ] **Step 5: Register auto-configuration**

Use `@ConditionalOnProperty(prefix="lab.message.local", name="enabled", havingValue="true", matchIfMissing=true)` and `@ConditionalOnMissingBean(EventPublisher.class)` so applications can replace the local publisher.

- [ ] **Step 6: Compile the local starter**

Run: `mvn -f platform-message/message-local-starter/pom.xml -DskipTests compile`

Expected: `BUILD SUCCESS`.

- [ ] **Step 7: Commit the local starter**

```bash
git add platform-message/message-local-starter
git commit -m "feat: add local message starter"
```

### Task 5: Implement RocketMQ Adapter

**Files:**
- Modify: `platform-message/message-rocketmq-adapter/pom.xml`
- Create: `platform-message/message-rocketmq-adapter/src/main/java/com/lab/message/rocketmq/adapter/RocketMqMessageMapper.java`
- Create: `platform-message/message-rocketmq-adapter/src/main/java/com/lab/message/rocketmq/adapter/RocketMqProducer.java`
- Create: `platform-message/message-rocketmq-adapter/src/main/java/com/lab/message/rocketmq/adapter/RocketMqPublishResultMapper.java`
- Create: `platform-message/message-rocketmq-adapter/src/main/java/com/lab/message/rocketmq/adapter/RocketMqConfiguration.java`

**Interfaces:**
- Consumes `EventSerializer`, `MessageNamingStrategy`, and RocketMQ configuration values.
- Produces ordinary, ordered, delayed, and transactional adapter methods without exposing SDK types through `message-contract`.

- [ ] **Step 1: Add only RocketMQ adapter dependencies**

Use the Spring Cloud Alibaba RocketMQ dependency managed by the project BOM where available; otherwise use the exact project-approved RocketMQ version in the adapter only. Do not add RocketMQ dependencies to contract or core.

- [ ] **Step 2: Implement envelope-to-native-message mapping**

Map serialized bytes to RocketMQ body, event ID to key/message key, event type to tag when configured, traceparent to native user properties, and configured destination to topic.

- [ ] **Step 3: Implement ordinary send**

Use the native producer API to send synchronously with configured timeout and retry settings. Map the native message ID and send status to `PublishResult`.

- [ ] **Step 4: Implement ordered send**

Select a queue using `partitionKey` consistently. Reject blank partition keys instead of silently losing ordering semantics.

- [ ] **Step 5: Implement delayed send**

Map the requested delay to an explicit configured RocketMQ delay level. Reject unsupported durations rather than rounding silently.

- [ ] **Step 6: Implement transactional send boundary**

Expose a transaction callback/adapter SPI rather than pretending a plain publisher call is a transaction. The adapter must require a transaction listener or callback and map unknown transaction state to RocketMQ’s check mechanism.

- [ ] **Step 7: Compile the adapter**

Run: `mvn -f platform-message/message-rocketmq-adapter/pom.xml -DskipTests compile`

Expected: `BUILD SUCCESS`.

- [ ] **Step 8: Commit the adapter**

```bash
git add platform-message/message-rocketmq-adapter
git commit -m "feat: add rocketmq message adapter"
```

### Task 6: Implement RocketMQ Starter

**Files:**
- Modify: `platform-message/message-rocketmq-starter/pom.xml`
- Create: `platform-message/message-rocketmq-starter/src/main/java/com/lab/message/rocketmq/RocketMqMessageProperties.java`
- Create: `platform-message/message-rocketmq-starter/src/main/java/com/lab/message/rocketmq/RocketMqMessageAutoConfiguration.java`
- Create: `platform-message/message-rocketmq-starter/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

**Interfaces:**
- Produces `EventPublisher` and only exposes `OrderedEventPublisher`, `DelayedEventPublisher`, and `TransactionalEventPublisher` when their required adapter support is configured.
- Allows replacement of serializer, naming strategy, producer, and publisher beans.

- [ ] **Step 1: Define properties**

Bind `lab.message.rocketmq.enabled`, name server, producer group, send timeout, retry settings, consumer group/thread settings, naming prefixes, and delay-level mappings. Every property must feed the adapter configuration.

- [ ] **Step 2: Implement conditional auto-configuration**

Use `@ConditionalOnClass` for RocketMQ classes, `@ConditionalOnProperty` for the feature switch, and `@ConditionalOnMissingBean` for replaceable publishers. Do not create RocketMQ clients when the feature is disabled.

- [ ] **Step 3: Make transport selection explicit**

Do not register RocketMQ `EventPublisher` when local transport is the selected default unless the application explicitly enables RocketMQ. Avoid ambiguous `EventPublisher` beans when Local and RocketMQ starters are both present by using a configured primary transport or a named publisher contract.

- [ ] **Step 4: Compile the RocketMQ starter**

Run: `mvn -f platform-message/message-rocketmq-starter/pom.xml -DskipTests compile`

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit the starter**

```bash
git add platform-message/message-rocketmq-starter
git commit -m "feat: add rocketmq message starter"
```

### Task 7: Add Message Lab

**Files:**
- Modify: `platform-message/message-lab/pom.xml`
- Create: `platform-message/message-lab/src/main/java/com/lab/message/lab/MessageLabApplication.java`
- Create: `platform-message/message-lab/src/main/java/com/lab/message/lab/MessageProbeController.java`
- Create: `platform-message/message-lab/src/main/resources/application.yml`

**Interfaces:**
- Consumes `EventPublisher` and optional explicit capability interfaces.
- Demonstrates protocol creation, local sync/async dispatch, and compile-time RocketMQ capability wiring without implementing Outbox or Inbox.

- [ ] **Step 1: Add application dependencies**

Depend on `message-contract`, `message-core`, `message-local-starter`, and `message-rocketmq-starter`. Do not add `capability-reliable-message`.

- [ ] **Step 2: Add a protocol probe**

Expose a small controller or runner that creates an `EventEnvelope` through `EventEnvelopeFactory`, publishes it through `EventPublisher`, and returns `PublishResult`. Do not hard-code broker SDK calls in the Lab.

- [ ] **Step 3: Add separate explicit capability probes**

Use distinct methods for ordered, delayed, and transactional publisher interfaces. Do not cast `EventPublisher` to capabilities.

- [ ] **Step 4: Compile the Lab**

Run: `mvn -f platform-message/message-lab/pom.xml -DskipTests compile`

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit the Lab**

```bash
git add platform-message/message-lab
git commit -m "feat: add message lab"
```

### Task 8: Full Reactor Verification

**Files:**
- Modify: only files required by compiler or dependency convergence failures.

- [ ] **Step 1: Run the full package build**

Run: `mvn clean package -DskipTests`

Expected: all existing modules plus the six `platform-message` children finish with `BUILD SUCCESS`.

- [ ] **Step 2: Check stale scope and dependency leaks**

Run:

```bash
rg "RocketMQ|Kafka|RabbitMQ|spring-cloud-stream" platform-message/message-contract platform-message/message-core
rg "capability-reliable-message|outbox|inbox|idempot" platform-message
git diff --check
```

Expected: no broker/framework references in contract/core, no reliable-message implementation in platform-message, and no whitespace errors.

- [ ] **Step 3: Inspect final status**

Run: `git status --short`

Confirm unrelated user staged/deleted test changes remain untouched.

- [ ] **Step 4: Final review checkpoint**

Review that every property is consumed, every Starter has `AutoConfiguration.imports`, and Local and RocketMQ publish through the same `EventPublisher` contract while explicit RocketMQ capabilities remain separate.
