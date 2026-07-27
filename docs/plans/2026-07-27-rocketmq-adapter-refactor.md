# RocketMQ Adapter Refactor Implementation Plan

> **REQUIRED SUB-SKILL:** Use the executing-plans skill to implement this plan task-by-task.

**Goal:** Simplify the RocketMQ message adapter by removing its delegate facade, isolating RocketMQ configuration, and making consumer settings type-safe.

**Architecture:** The auto-configuration will publish `RocketMqEventPublisher` and `RocketMqEventSubscriber` directly as the contract beans. RocketMQ-specific consumer options will reside in a nested typed properties class under `lab.message.rocketmq.adapter`; the annotation factory will map that object to the complete RocketMQ listener annotation attribute map. Header names become constants local to the RocketMQ adapter.

**Tech Stack:** Java 21, Spring Boot configuration properties, Apache RocketMQ Spring, Maven.

---

### Task 1: Remove the delegate facade

**Files:**
- Delete: `platform-message/message-rocketmq-adapter/src/main/java/com/lab/message/rocketmq/adapter/RocketMqMessageFacade.java`
- Modify: `platform-message/message-rocketmq-adapter/src/main/java/com/lab/message/rocketmq/adapter/config/RocketMqAdapterAutoConfiguration.java`

**Steps:**
1. Register concrete publisher and subscriber beans.
2. Expose each through `EventPublisher` and `EventSubscriber` contracts without a facade.
3. Make the consumer registrar conditional on the subscriber contract.

### Task 2: Type the RocketMQ consumer configuration

**Files:**
- Modify: `platform-message/message-rocketmq-adapter/src/main/java/com/lab/message/rocketmq/adapter/config/RocketMqAdapterProperties.java`
- Modify: `platform-message/message-rocketmq-adapter/src/main/java/com/lab/message/rocketmq/adapter/RocketMqListenerAnnotationFactory.java`
- Modify: `platform-message/message-rocketmq-adapter/src/main/java/com/lab/message/rocketmq/adapter/RocketMqEventSubscriber.java`
- Modify: `platform-message/message-lab/src/main/resources/application-rocketmq.yml`

**Steps:**
1. Change the properties prefix to `lab.message.rocketmq.adapter`.
2. Replace the raw third-level attribute map with `RocketMqConsumerProperties` typed fields.
3. Change lookup to return the typed object.
4. Make the annotation factory consume typed properties and map every annotation attribute explicitly.
5. Convert the sample YAML to kebab-case typed fields.

### Task 3: Centralize RocketMQ event headers

**Files:**
- Create: `platform-message/message-rocketmq-adapter/src/main/java/com/lab/message/rocketmq/adapter/RocketMqMessageHeaders.java`
- Modify: `platform-message/message-rocketmq-adapter/src/main/java/com/lab/message/rocketmq/adapter/RocketMqMessageMapper.java`

**Steps:**
1. Define the adapter-owned `lab.*` header constants.
2. Replace mapper string literals with constants.

### Task 4: Verify compilation

**Files:** none

**Steps:**
1. Confirm old facade, raw consumer maps, and generic adapter configuration prefix no longer occur in production code.
2. Run Maven compile for contract, local starter, RocketMQ adapter, and message lab:
   `mvn -pl platform-message/message-contract,platform-message/message-local-starter,platform-message/message-rocketmq-adapter,platform-message/message-lab -am -DskipTests compile`
