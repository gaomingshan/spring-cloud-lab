package com.lab.governance.feign;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import feign.Logger;

@ConfigurationProperties("lab.governance.feign")
@Getter
@Setter
public class FeignProperties {
    private boolean enabled = true;
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(10);
    private boolean requestIdPropagation = true;
    private boolean traceContextPropagation = true;
    private boolean authorizationPropagation = true;
    private boolean httpClient5Enabled = true;
    private int maxConnections = 200;
    private int maxConnectionsPerRoute = 50;
    private Logger.Level loggerLevel = Logger.Level.BASIC;

}
