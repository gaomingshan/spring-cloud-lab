# Remove Message Schema Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove schema-version and Upcaster enforcement from the messaging foundation so JSON payload evolution remains an application-level concern.

**Architecture:** Keep `EventEnvelope` focused on transport and tracing metadata plus an opaque JSON-compatible payload. `message-core` will provide JSON serialization, validation, context propagation, and naming only; clients may implement their own payload versioning through payload fields or headers. RocketMQ mapping will no longer publish schema metadata as a native property.

**Tech Stack:** Java 21, Spring Boot 3.5.9, Jackson, Maven, existing broker-neutral message modules.

## Global Constraints

- `message-contract` and `message-core` remain free of RocketMQ, Kafka, RabbitMQ, and Spring Cloud Stream types.
- No Outbox, Inbox, durable local messaging, consumer idempotency, or eventual-consistency workflow is added.
- Schema versioning and Upcaster behavior are removed rather than deprecated or replaced by compatibility shims.
- JSON serialization must continue to validate event identity, event type, producer, traceparent, and payload.
- Existing unrelated staged/deleted governance test changes remain untouched.
- Verification uses `mvn clean package -DskipTests` because the root Maven configuration skips test compilation and execution.

---

### Task 1: Remove Schema From Public Contract and Core Serialization

**Files:**
- Modify: `platform-message/message-contract/src/main/java/com/lab/message/contract/EventEnvelope.java`
- Modify: `platform-message/message-core/src/main/java/com/lab/message/core/EventSerializer.java`
- Modify: `platform-message/message-core/src/main/java/com/lab/message/core/JsonEventSerializer.java`
- Modify: `platform-message/message-core/src/main/java/com/lab/message/core/EventEnvelopeFactory.java`
- Modify: `platform-message/message-core/src/main/java/com/lab/message/core/MessageCoreProperties.java`
- Delete: `platform-message/message-core/src/main/java/com/lab/message/core/SchemaUpcaster.java`
- Delete: `platform-message/message-core/src/main/java/com/lab/message/core/SchemaUpcasterRegistry.java`

**Interfaces:**
- `EventEnvelope<T>` no longer contains `schemaVersion`.
- `EventSerializer` exposes only `serialize(EventEnvelope<?>)` and `deserialize(byte[], Class<T>)`.
- `EventEnvelopeFactory.create(eventType, payload)` no longer reads schema configuration.
- `JsonEventSerializer` performs direct JSON decoding without upgrade paths.

- [ ] **Step 1: Remove schema fields and APIs**

Remove `schemaVersion` from the record and all constructor calls. Remove `defaultSchemaVersion` accessors and validation. Remove `deserializeAndUpgrade(...)` from the serializer interface and implementation.

- [ ] **Step 2: Simplify JSON deserialization**

Read `eventId`, `eventType`, `producer`, aggregate fields, `occurredAt`, `traceparent`, `headers`, and `payload` directly from JSON. Keep validation for blank identity fields, malformed traceparent, and null payload. Do not read or require a `schemaVersion` JSON field.

- [ ] **Step 3: Delete Upcaster classes**

Delete `SchemaUpcaster.java` and `SchemaUpcasterRegistry.java`; do not leave forwarding or deprecated compatibility classes.

- [ ] **Step 4: Compile the affected modules**

Run: `mvn -f platform-message/pom.xml -DskipTests compile`

Expected: `BUILD SUCCESS` for all seven platform-message modules.

### Task 2: Remove Schema From RocketMQ and Lab

**Files:**
- Modify: `platform-message/message-rocketmq-adapter/src/main/java/com/lab/message/rocketmq/adapter/RocketMqMessageMapper.java`
- Modify: `platform-message/message-lab/src/main/java/com/lab/message/lab/MessageLabApplication.java`
- Modify: `platform-message/message-lab/src/main/java/com/lab/message/lab/MessageProbeController.java`
- Modify: `platform-message/message-lab/src/main/resources/application.yml`

**Interfaces:**
- RocketMQ user properties retain event identity and trace metadata but no schema property.
- Lab creates envelopes through the simplified `EventEnvelopeFactory` and publishes unchanged JSON payloads.

- [ ] **Step 1: Remove RocketMQ schema mapping**

Delete the `schemaVersion` native property write and remove it from any reserved envelope-property set if it is no longer a canonical envelope field.

- [ ] **Step 2: Update Lab call sites**

Adjust all envelope construction and serializer usage to the new signatures. Keep ordinary, ordered, delayed, and transactional probes unchanged in purpose.

- [ ] **Step 3: Compile the Lab and adapter**

Run: `mvn -f platform-message/pom.xml -DskipTests compile`

Expected: `BUILD SUCCESS`.

### Task 3: Update Design and Selection Documentation

**Files:**
- Modify: `docs/superpowers/specs/2026-07-18-platform-message-design.md`
- Modify: `docs/superpowers/plans/2026-07-18-platform-message-implementation.md`
- Modify: `技术选型.md`
- Modify: `分布式技术基座架构设计.md`

- [ ] **Step 1: Remove schema requirements**

Remove schema-version fields, Upcaster references, schema validation requirements, and schema-upgrade Lab claims.

- [ ] **Step 2: Add the new boundary**

Document that the foundation treats payload as JSON-compatible opaque data. Applications may carry their own version marker in payload or application-owned headers when needed; the core does not interpret or enforce it.

- [ ] **Step 3: Search documentation and source for stale terms**

Run searches for `schemaVersion`, `SchemaUpcaster`, `deserializeAndUpgrade`, `defaultSchemaVersion`, and `schema-version` across the repository. Expected: no matches in implementation or active design documents.

### Task 4: Full Verification

**Files:**
- Modify: only files required by compilation or stale-reference cleanup.

- [ ] **Step 1: Run the full reactor build**

Run: `mvn clean package -DskipTests`

Expected: `BUILD SUCCESS` for all 37 reactor modules.

- [ ] **Step 2: Check forbidden dependencies and scope leaks**

Run:

```text
git diff --check
```

Search `message-contract` and `message-core` for `RocketMQ`, `Kafka`, `RabbitMQ`, and `spring-cloud-stream`. Search `platform-message` for `outbox`, `inbox`, and `capability-reliable-message`.

- [ ] **Step 3: Confirm Schema removal**

Search the repository for `schemaVersion`, `SchemaUpcaster`, `deserializeAndUpgrade`, `defaultSchemaVersion`, and `schema-version`. Any remaining match must be either historical Git metadata or an explicitly unrelated document; active source and design documentation must have none.

- [ ] **Step 4: Inspect worktree status**

Run: `git status --short`

Confirm the pre-existing staged/deleted governance test changes remain untouched.
