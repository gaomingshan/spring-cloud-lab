# RocketMQ Spring Bridge Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace the custom RocketMQ client implementation with a thin bridge over the official RocketMQ Spring integration.

**Architecture:** `rocketmq-spring-boot-starter:2.3.2` owns RocketMQ client creation, configuration binding, producer and consumer lifecycle, message conversion, listener containers, retries, consume models, and consumer threads. Our adapter only converts `EventEnvelope` to Spring `Message`, delegates to `RocketMQTemplate`, and exposes the existing publisher capability interfaces.

**Tech Stack:** Java 21, Spring Boot 3.5.9, RocketMQ Spring Boot 2.3.2, Apache RocketMQ client transitively supplied by RocketMQ Spring.

## Global Constraints

- Official `rocketmq.*` properties are the source of truth for RocketMQ client configuration.
- `lab.message.rocketmq.*` only controls adapter enablement, delay-level lookup, and the optional destination resolver.
- Do not create a custom Producer, Consumer, Codec, MessageConverter, ListenerContainer, retry policy, consume model, or thread pool.
- User-provided official RocketMQ beans and adapter resolver beans override defaults with `@ConditionalOnMissingBean`.
- Business code depends on `EventSubscriber`, `EventSubscription`, and `EventHandler`; it does not use `@RocketMQMessageListener` directly.
- Verify with `mvn clean package -DskipTests` and `git diff --check`.

### Task 1: Use Official RocketMQ Spring Dependencies

**Files:**
- Modify: `platform-message/message-rocketmq-adapter/pom.xml`
- Modify: `platform-message/message-rocketmq-starter/pom.xml`

- [x] Keep `message-rocketmq-adapter` dependent on `rocketmq-spring-boot` only for bridge APIs.
- [x] Keep `message-rocketmq-starter` dependent on `rocketmq-spring-boot-starter` for official auto-configuration.
- [x] Remove direct ownership of `rocketmq-client` from project modules.

### Task 2: Delegate Publishing to RocketMQTemplate

**Files:**
- Modify: `message-rocketmq-adapter/.../RocketMqTransport.java`
- Modify: `message-rocketmq-adapter/.../RocketMqMessageMapper.java`
- Add: `message-rocketmq-adapter/.../RocketMqDestinationResolver.java`

- [x] Map complete envelopes to Spring `Message` with official `RocketMQHeaders`.
- [x] Delegate ordinary, ordered, delayed, and transaction sends to `RocketMQTemplate`.
- [x] Preserve `MessageException` translation only at the facade boundary.
- [x] Allow a user-provided destination resolver to control topic mapping.

### Task 3: Reuse Official Configuration and Listener Ecosystem

**Files:**
- Modify: `message-rocketmq-starter/.../RocketMqMessageProperties.java`
- Modify: `message-rocketmq-starter/.../RocketMqMessageAutoConfiguration.java`
- Modify: `message-lab/src/main/resources/application.yml`

- [x] Remove duplicated producer, consumer, transaction, name-server, retry, and thread settings from custom properties.
- [x] Keep official `rocketmq.*` configuration as the source for Producer and Consumer behavior.
- [x] Keep adapter-specific delay levels and optional topic prefix/resolver only.
- [x] Gate the adapter facade on the presence of official `RocketMQTemplate` and the lab enablement property.
- [x] Bridge `EventSubscriber.subscribe(...)` through official `DefaultRocketMQListenerContainer` lifecycle and `RocketMQMessageConverter`.

### Task 4: Verify and Document

**Files:**
- Modify: `docs/superpowers/specs/2026-07-18-platform-message-design.md`
- Modify: `docs/superpowers/plans/2026-07-18-remove-message-core.md`

- [x] Document official `rocketmq.*` producer and consumer properties, `@RocketMQMessageListener`, `ConsumeMode`, and `MessageModel` as the supported configuration surface.
- [x] Run module and root Maven builds.
- [x] Run stale reference scans and `git diff --check`.
