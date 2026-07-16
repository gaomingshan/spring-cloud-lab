package com.lab.governance.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("lab.governance.gateway")
public class GatewayProperties {
    private boolean enabled = true;
    private String requestIdHeader = "X-Request-Id";
    private Duration defaultConnectTimeout = Duration.ofSeconds(3);
    private Duration defaultResponseTimeout = Duration.ofSeconds(10);

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getRequestIdHeader() { return requestIdHeader; }
    public void setRequestIdHeader(String requestIdHeader) { this.requestIdHeader = requestIdHeader; }
    public Duration getDefaultConnectTimeout() { return defaultConnectTimeout; }
    public void setDefaultConnectTimeout(Duration value) { this.defaultConnectTimeout = value; }
    public Duration getDefaultResponseTimeout() { return defaultResponseTimeout; }
    public void setDefaultResponseTimeout(Duration value) { this.defaultResponseTimeout = value; }
}
