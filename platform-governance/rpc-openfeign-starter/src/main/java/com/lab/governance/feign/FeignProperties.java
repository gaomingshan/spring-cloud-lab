package com.lab.governance.feign;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("lab.governance.feign")
public class FeignProperties {
    private boolean enabled = true;
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(10);
    private boolean requestIdPropagation = true;
    private boolean traceContextPropagation = true;
    private boolean authorizationPropagation = true;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Duration getConnectTimeout() { return connectTimeout; }
    public void setConnectTimeout(Duration value) { this.connectTimeout = value; }
    public Duration getReadTimeout() { return readTimeout; }
    public void setReadTimeout(Duration value) { this.readTimeout = value; }
    public boolean isRequestIdPropagation() { return requestIdPropagation; }
    public void setRequestIdPropagation(boolean value) { this.requestIdPropagation = value; }
    public boolean isTraceContextPropagation() { return traceContextPropagation; }
    public void setTraceContextPropagation(boolean value) { this.traceContextPropagation = value; }
    public boolean isAuthorizationPropagation() { return authorizationPropagation; }
    public void setAuthorizationPropagation(boolean value) { this.authorizationPropagation = value; }
}
