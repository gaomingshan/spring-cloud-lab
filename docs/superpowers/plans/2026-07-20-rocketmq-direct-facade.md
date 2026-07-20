# RocketMQ Direct Facade Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace the proxy and manually managed listener-container design with one small, directly implemented RocketMQ facade over official RocketMQ Spring components.

**Architecture:** `RocketMqMessageFacade` directly implements the broker-neutral publisher and subscriber interfaces. It delegates sending to `RocketMQTemplate`, maps messages through the existing adapter mapper, and registers subscriptions through the official `RocketMQMessageListenerContainerRegistrar`; no dynamic proxy, custom producer, consumer, thread pool, retry policy, or lifecycle collection remains.

**Tech Stack:** Java 21, Spring Boot 3.5.9, RocketMQ Spring Boot 2.3.2, `RocketMQTemplate`, `RocketMQMessageConverter`, and official listener registrar.

## Global Constraints

- A one-to-one interface translation is implemented directly, not with dynamic proxies.
- Official RocketMQ Spring components own producer, consumer, conversion, retry, thread, and lifecycle behavior.
- Unsupported capabilities throw `MessageException`.
- Business-facing APIs contain no RocketMQ types.
- Preserve existing unrelated staged/deleted governance test changes.
- Verify with `mvn clean package -DskipTests`, `git diff --check`, and stale-reference scans.

### Task 1: Replace Publisher Wrappers With One Direct Facade

**Files:**
- Add: `platform-message/message-rocketmq-adapter/src/main/java/com/lab/message/rocketmq/adapter/RocketMqMessageFacade.java`
- Delete: `RocketMqEventPublisher.java`
- Delete: `RocketMqOrderedProducer.java`
- Delete: `RocketMqDelayedProducer.java`
- Delete: `RocketMqTransactionalProducer.java`
- Modify: `RocketMqMessageAutoConfiguration.java`

- [ ] Implement all five broker-neutral interfaces directly on `RocketMqMessageFacade`.
- [ ] Delegate ordinary, ordered, delayed, and transaction publishing to `RocketMQTemplate`.
- [ ] Throw `MessageException` when delay configuration or transaction integration is unavailable.
- [ ] Register one facade bean and expose it through Spring by its implemented interfaces; do not create proxy beans.

### Task 2: Replace Dynamic Container Management With Official Registrar

**Files:**
- Modify: `RocketMqMessageFacade.java`
- Delete: `RocketMqEventSubscriber.java`
- Add: `RocketMqSubscriptionRegistrar.java` only if a thin constructor adapter is required by the official registrar API.

- [ ] Inject `RocketMQMessageListenerContainerRegistrar` and `RocketMQMessageConverter` from the official starter.
- [ ] Convert `EventSubscription` into the official listener annotation configuration.
- [ ] Register a small concrete RocketMQ listener delegate with the official registrar; do not instantiate or store `DefaultRocketMQListenerContainer`.
- [ ] Let official Spring lifecycle start and stop registered containers.
- [ ] Preserve `EventHandler` as the only callback visible to business code.

### Task 3: Remove Obsolete Bridge State and Synchronize Documentation

**Files:**
- Modify: `RocketMqTransport.java` or delete it if its methods move unchanged into the facade.
- Modify: `RocketMqMessageMapper.java` only for facade use.
- Modify: `docs/superpowers/specs/2026-07-18-platform-message-design.md`
- Modify: `docs/superpowers/plans/2026-07-20-rocketmq-spring-bridge.md`

- [ ] Remove dynamic proxy, annotation proxy, container collection, manual bean registration, and custom close logic.
- [ ] Keep `RocketMqDestinationResolver` as the only adapter extension point beyond the direct facade.
- [ ] Document direct facade implementation and official registrar delegation.

### Task 4: Verify

- [ ] Run `mvn -f platform-message/pom.xml clean package -DskipTests`.
- [ ] Run `mvn clean package -DskipTests`.
- [ ] Run `git diff --check`.
- [ ] Confirm no `Proxy`, `InvocationHandler`, `registerSingleton`, `DefaultRocketMQListenerContainer`, custom producer, or custom thread-pool implementation remains in the adapter.
