package com.lab.governance.discovery;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("lab.governance.discovery")
public class ServiceDiscoveryProperties {
    private boolean enabled = true;
    private String namespace;
    private String serverAddr;
    private String username;
    private String password;
    private String group = "DEFAULT_GROUP";
    private String cluster = "DEFAULT";
}
