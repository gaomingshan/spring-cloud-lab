# Governance Capability Redesign

## Goal

Turn `platform-governance` modules into useful, demonstrable platform capabilities. Spring Boot auto-configuration and Spring SPI are implementation mechanisms only; each module must reduce application code, provide a stable interface, or compose lower-level components into a higher-level governance behavior.

## Delivery Scope

The first delivery provides runnable core loops. Production enhancements such as richer metrics, advanced retry policies, complete tracing propagation, and broader customization follow afterward.

The first delivery must demonstrate:

- Dynamic configuration reads and change listeners.
- Dynamic service-instance lookup and change listeners.
- Gateway request-context and error governance.
- Feign timeout, HTTP client, codec, interceptor, and error-decoding configuration.
- Sentinel dynamic rules, Feign rate limiting, circuit breaking, and fallback.

`governance-lab` must exercise these behaviors through application endpoints and real component interactions.

## Module Boundaries

The modules will be renamed as follows:

```text
nacos-config-starter       -> config-center-starter
nacos-discovery-starter    -> service-discovery-starter
gateway-starter            -> gateway-governance-starter
rpc-openfeign-starter      -> feign-governance-starter
sentinel-starter           -> sentinel-governance-starter
```

`governance-contract` remains the shared contract module. It will contain shared context, header, service-instance, configuration-change, and extension types until independent publication requires further splitting.

## Configuration Center

`config-center-starter` hides the Nacos Config client behind a platform API:

```java
public interface DynamicConfigService {
    String get(String dataId, String group);
    <T> T get(String dataId, String group, Class<T> type);
    void addListener(String dataId, String group, ConfigChangeListener listener);
}
```

The default Nacos implementation supports data ID, group, namespace, dynamic reads, listeners, and a local last-known-value cache. Initial-read failure prevents startup; runtime update failure retains the previous value and is logged. The Lab exposes an endpoint that returns the current value so a Nacos change can be observed without restarting the application.

## Service Discovery

`service-discovery-starter` hides the Nacos naming client behind a platform API:

```java
public interface ServiceDiscovery {
    List<ServiceInstanceView> getInstances(String serviceId);
    Optional<ServiceInstanceView> choose(String serviceId);
    void addListener(String serviceId, ServiceChangeListener listener);
}
```

The default implementation supports namespace, group, cluster, health and weight information, basic instance selection, and instance-change listeners. The Lab exposes an endpoint showing the current instances for a service. This API also supplies the discovery foundation used by future RPC integrations.

## Gateway Governance

`gateway-governance-starter` remains a reusable Gateway Starter, not a standalone gateway application. A standalone application belongs in a Lab or scenario module.

The first delivery provides:

- Request ID generation, validation, and response-header propagation.
- Trace and principal context extraction into the shared request context.
- Consistent downstream header propagation.
- A configurable filter order.
- A standard gateway error response.
- Gateway HTTP client connect and response timeout configuration that is actually applied.
- Extension points for custom header extraction and request-context handling.

## Feign Governance

`feign-governance-starter` becomes a real OpenFeign integration instead of only registering an interceptor. It provides default, replaceable components for:

- Feign client connection and read timeouts.
- Apache HttpClient 5 connection pooling and client customization.
- Spring-based encoder and decoder composition.
- Unified error decoding and empty-response handling.
- Request context propagation.
- Retry behavior and client-level overrides.
- Request/response logging hooks with sensitive-data handling extension points.
- Feign client metrics and stable resource names for Sentinel integration.

The starter uses Spring Cloud OpenFeign's supported extension points such as `Encoder`, `Decoder`, `ErrorDecoder`, `Retryer`, `RequestInterceptor`, HTTP client customizers, and per-client configuration. It does not duplicate mature Spring Cloud codec internals without adding a governance behavior. Every default component is replaceable by a user Bean or explicit client configuration.

## Sentinel Governance

`sentinel-governance-starter` provides the dynamic rule and Feign protection layer:

- Nacos DataSource auto-configuration for flow and degrade rules.
- Configurable namespace, group, and data IDs.
- Dynamic rule loading with last-known rules retained after runtime update failure.
- Stable Feign resource naming.
- Feign call rate limiting before network execution.
- Circuit breaking after configured failure conditions.
- Default fallback response with user replacement hooks.
- Current rule/status visibility for the Lab.

Feign remains responsible for sending and decoding HTTP calls. Sentinel decides whether a call may proceed and whether a failure should open a circuit or execute fallback. The Sentinel module detects the Feign integration when present, rather than forcing the base Feign module to depend on Sentinel.

The target call flow is:

```text
Controller -> Feign Client -> Sentinel entry -> rate limit/circuit breaker -> HTTP client
                                      -> error decoder/fallback
```

## Lab Demonstration

`governance-lab` will provide demonstrations for:

```text
GET /governance/config/{dataId}
GET /governance/services/{serviceId}
GET /governance/feign/{serviceId}
GET /governance/sentinel/status
```

The complete demonstration changes a Nacos configuration at runtime, queries registered instances, invokes a discovered service through Feign, propagates governance context, applies Sentinel rate limiting, opens a circuit after failures, and returns the fallback using the shared response contract.

## Validation

Build with `mvn clean package -DskipTests`. Do not use tests as the integration mechanism. Run the Lab with the existing Nacos environment and verify the endpoints and runtime behavior manually. The build must not depend on secrets committed to the repository.

## Out of Scope

- A standalone production gateway application.
- Splitting every contract into a separate Maven module before independent publication is needed.
- Changing the Spring Boot, Spring Cloud, Java, or platform BOM baseline.
- Production-grade retry, tracing, metrics, and operational hardening beyond the first runnable core loop.
