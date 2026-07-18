# Local Spring Event Bridge Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax.

**Goal:** Replace the custom local event bus with a thin Spring `ApplicationEventPublisher` bridge.

**Architecture:** `message-local-starter` will translate `EventEnvelope<?>` into a local Spring application event and delegate dispatch to Spring's event infrastructure. The starter will not own handler registries, executors, async policies, or listener lifecycle. Applications receive local messages through Spring `@EventListener`; `message-contract` will no longer expose a parallel `EventHandler` abstraction.

**Tech Stack:** Java 21, Spring Boot 3.5.9, Spring `spring-context`, existing message contract/core modules.

## Global Constraints

- Do not implement a second local event bus, handler registry, executor, or listener lifecycle.
- `message-contract` must not depend on Spring types.
- `message-local-starter` may depend on `spring-context` and owns the Spring bridge type.
- Keep the existing `EventPublisher.publish(EventEnvelope<?>)` contract and exception-based failure semantics.
- Do not modify RocketMQ implementation in this task.
- Do not run Maven compilation or tests until the interface and implementation migration is complete.

---

### Task 1: Replace Local Bus With Spring Event Bridge

**Files:**
- Create: `platform-message/message-local-starter/src/main/java/com/lab/message/local/LocalMessageEvent.java`
- Modify: `platform-message/message-local-starter/src/main/java/com/lab/message/local/LocalEventPublisher.java`
- Modify: `platform-message/message-local-starter/src/main/java/com/lab/message/local/LocalMessageAutoConfiguration.java`
- Modify: `platform-message/message-local-starter/pom.xml`
- Delete: `platform-message/message-local-starter/src/main/java/com/lab/message/local/LocalEventHandlerRegistry.java`

**Interfaces:**
- `LocalMessageEvent` extends `ApplicationEvent` and exposes `EventEnvelope<?> envelope()`.
- `LocalEventPublisher implements EventPublisher` and delegates to `ApplicationEventPublisher`.

- [ ] **Step 1: Add the Spring event carrier**

Implement:

```java
public final class LocalMessageEvent extends ApplicationEvent {
    private final EventEnvelope<?> envelope;

    public LocalMessageEvent(Object source, EventEnvelope<?> envelope) {
        super(source);
        this.envelope = envelope;
    }

    public EventEnvelope<?> envelope() {
        return envelope;
    }
}
```

- [ ] **Step 2: Replace publisher internals**

Make `LocalEventPublisher` accept `ApplicationEventPublisher`, reject only a null envelope, then call `applicationEventPublisher.publishEvent(new LocalMessageEvent(this, event))`. Let synchronous listener exceptions propagate; do not catch them into result objects. Remove executor, dispatch mode, registry, and lifecycle code.

- [ ] **Step 3: Simplify auto-configuration**

Register only the `LocalEventPublisher` bean with `@ConditionalOnMissingBean(EventPublisher.class)`. Keep the existing Local enablement and RocketMQ mutual exclusion condition. Remove the Registry bean and `destroyMethod`.

- [ ] **Step 4: Update dependencies and properties**

Keep `spring-boot-autoconfigure`, add `org.springframework:spring-context`, and remove the obsolete `LocalMessageProperties` class and its `@EnableConfigurationProperties` usage. No local executor configuration remains.

- [ ] **Step 5: Delete the parallel handler abstraction**

Delete `EventHandler.java` from `message-contract` and remove all Registry references. Spring `@EventListener` is the only local receive mechanism in this slice.

### Task 2: Update Lab Wiring and Documentation

**Files:**
- Modify: `platform-message/message-lab/src/main/java/com/lab/message/lab/MessageLabApplication.java`
- Modify: `platform-message/message-lab/src/main/java/com/lab/message/lab/MessageProbeController.java`
- Modify: `platform-message/message-lab/src/main/resources/application.yml`
- Modify: `技术选型.md`
- Modify: `docs/superpowers/specs/2026-07-18-platform-message-design.md`

- [ ] **Step 1: Register a Spring listener in the Lab**

Replace the Registry bean with a component or `@EventListener` method that consumes `LocalMessageEvent`. Keep the listener minimal and demonstrate `event.envelope()` access.

- [ ] **Step 2: Remove obsolete Local properties**

Delete `dispatch-mode` and executor configuration from Lab YAML. Keep only the Local enablement switch if needed.

- [ ] **Step 3: Update documentation boundary**

Document that Local transport delegates publication and reception to Spring `ApplicationEventPublisher` and `@EventListener`; async behavior is controlled by application-level Spring `@Async`, not by a messaging-specific executor.

### Task 3: Verification

**Files:**
- Modify: only files required by compiler or stale-reference cleanup.

- [ ] **Step 1: Search stale custom-bus references**

Search `platform-message` for `LocalEventHandlerRegistry`, `ThreadPoolExecutor`, `PublishOptions`, `PublishResult`, `PublishStatus`, and `EventHandler`. Expected: no Local-bus or removed publish-result references in the migrated Local/Lab code; RocketMQ migration remains a separate follow-up.

- [ ] **Step 2: Compile after migration**

Run: `mvn -f platform-message/pom.xml clean package -DskipTests`

Expected: `BUILD SUCCESS` after all affected interface implementations are migrated.

- [ ] **Step 3: Check formatting and status**

Run: `git diff --check` and `git status --short`. Confirm unrelated staged/deleted governance tests remain untouched.
