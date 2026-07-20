# Remove Message Core Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Remove the artificial `message-core` implementation layer and let callers create `EventEnvelope` while each adapter delegates serialization, naming, and transport behavior to its native ecosystem.

**Architecture:** `message-contract` remains the only broker-neutral API. Local delegates to Spring application events. RocketMQ delegates to the official RocketMQ Spring integration (`RocketMQTemplate`, `RocketMQMessageConverter`, listener annotations, and listener containers); the adapter only maps complete envelopes and exposes the common publisher facade. No new cross-broker codec, naming, factory, consumer, thread-pool, or properties abstraction is introduced.

**Tech Stack:** Java 21, Spring Boot 3.5.9, Maven, Jackson in the RocketMQ adapter, Apache RocketMQ client 5.3.1, Spring ApplicationEvent infrastructure.

## Global Constraints

- Callers construct `EventEnvelope` directly.
- `message-core` has no remaining implementation responsibility and is removed.
- Serialization and listener lifecycle are owned by the official RocketMQ Spring integration.
- Do not add a replacement validator, naming strategy, envelope factory, or cross-broker codec.
- Preserve existing staged/deleted governance test changes.
- Verify with `mvn clean package -DskipTests`, `git diff --check`, and stale-reference scans.

### Task 1: Remove Core Module

**Files:**
- Delete: `platform-message/message-core/src/main/java/com/lab/message/core/EventSerializer.java`
- Delete: `platform-message/message-core/src/main/java/com/lab/message/core/JsonEventSerializer.java`
- Delete: `platform-message/message-core/src/main/java/com/lab/message/core/EventEnvelopeFactory.java`
- Delete: `platform-message/message-core/src/main/java/com/lab/message/core/MessageNamingStrategy.java`
- Delete: `platform-message/message-core/src/main/java/com/lab/message/core/DefaultMessageNamingStrategy.java`
- Delete: `platform-message/message-core/src/main/java/com/lab/message/core/MessageCoreProperties.java`
- Delete: `platform-message/message-core/pom.xml`
- Modify: `platform-message/pom.xml`

- [ ] Remove the `message-core` module declaration and delete its implementation files.
- [ ] Confirm no Java or Maven source still imports a deleted Core type before moving to adapter changes.

### Task 2: Move RocketMQ Mapping to Native Adapter Dependencies

**Files:**
- Modify: `platform-message/message-rocketmq-adapter/pom.xml`
- Modify: `platform-message/message-rocketmq-adapter/src/main/java/com/lab/message/rocketmq/adapter/RocketMqMessageMapper.java`
- Add: `platform-message/message-rocketmq-adapter/src/main/java/com/lab/message/rocketmq/adapter/RocketMqEventCodec.java`
- Modify: `platform-message/message-rocketmq-starter/pom.xml`
- Modify: `platform-message/message-rocketmq-starter/src/main/java/com/lab/message/rocketmq/RocketMqMessageAutoConfiguration.java`

- [ ] Add Jackson dependencies directly to the RocketMQ adapter module and remove its `message-core` dependency.
- [ ] Implement `RocketMqEventCodec` as a Jackson bridge that encodes the complete envelope to bytes and validates only null/serialization failures through `MessageException`.
- [ ] Move destination derivation into RocketMQ-specific mapping using `RocketMqMessageProperties` topic prefix; preserve event type as the RocketMQ tag and event ID as key.
- [ ] Inject the adapter codec into `RocketMqMessageMapper`; do not expose a codec or naming interface through `message-contract`.
- [ ] Remove Core serializer/naming beans from RocketMQ auto-configuration.

### Task 3: Remove Core Usage from Local and Lab

**Files:**
- Modify: `platform-message/message-local-starter/src/main/java/com/lab/message/local/LocalEventPublisher.java`
- Modify: `platform-message/message-lab/src/main/java/com/lab/message/lab/MessageLabApplication.java`
- Modify: `platform-message/message-lab/src/main/java/com/lab/message/lab/MessageProbeController.java`
- Modify: `platform-message/message-lab/pom.xml`

- [ ] Remove `JsonEventSerializer.validate` usage; Local only rejects a null envelope before delegating to `ApplicationEventPublisher`.
- [ ] Remove the `EventEnvelopeFactory` bean and all Core imports from Lab.
- [ ] Construct complete envelopes directly in the Lab, including a non-empty `partitionKey` for ordered publishing.
- [ ] Remove the Lab dependency on `message-core`.

### Task 4: Synchronize Documentation and Verify

**Files:**
- Modify: `docs/superpowers/specs/2026-07-18-platform-message-design.md`
- Modify: `docs/superpowers/plans/2026-07-18-platform-message-implementation.md`
- Modify: `docs/superpowers/plans/2026-07-18-remove-message-schema.md`
- Modify: `docs/superpowers/plans/2026-07-18-local-spring-event-bridge.md`

- [ ] Describe `message-contract` as the broker-neutral layer and each adapter as owner of codec and naming.
- [ ] Remove claims that Core supplies JSON, naming, validation, or envelope creation.
- [ ] Run `mvn clean package -DskipTests` and require `BUILD SUCCESS`.
- [ ] Run `git diff --check` and scan Java/XML/docs for `message-core`, deleted type names, and Core imports.
