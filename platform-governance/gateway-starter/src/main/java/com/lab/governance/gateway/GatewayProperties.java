package com.lab.governance.gateway;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("lab.governance.gateway")
@Getter
@Setter
public class GatewayProperties {
    private boolean enabled = true;
    private String requestIdHeader = "X-Request-Id";
    private Duration defaultConnectTimeout = Duration.ofSeconds(3);
    private Duration defaultResponseTimeout = Duration.ofSeconds(10);

}
