package com.lab.governance.sentinel;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("lab.governance.sentinel")
@Getter
@Setter
public class SentinelProperties {
    private boolean enabled = true;
    private boolean eager;
    private String dashboard;
    private int transportPort = 8719;
    private final Nacos nacos = new Nacos();


    @Getter
    @Setter
    public static class Nacos {
        private boolean enabled;
        private String serverAddr;
        private String username;
        private String password;
        private String namespace;
        private String group = "DEFAULT_GROUP";
    }
}
