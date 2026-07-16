package com.lab.governance.lab;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    private String name;
    private String version;
    private String description;
}
