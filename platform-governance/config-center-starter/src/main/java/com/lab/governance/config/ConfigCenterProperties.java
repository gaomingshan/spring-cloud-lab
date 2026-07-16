package com.lab.governance.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("lab.governance.config")
public class ConfigCenterProperties {
    private boolean enabled = true;
    private String namespace;
    private String serverAddr;
    private String username;
    private String password;
}
