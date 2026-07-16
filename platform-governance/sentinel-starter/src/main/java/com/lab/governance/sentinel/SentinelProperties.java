package com.lab.governance.sentinel;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("lab.governance.sentinel")
public class SentinelProperties {
    private boolean enabled = true;
    private boolean eager;
    private String dashboard;
    private int transportPort = 8719;
    private final Nacos nacos = new Nacos();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isEager() { return eager; }
    public void setEager(boolean eager) { this.eager = eager; }
    public String getDashboard() { return dashboard; }
    public void setDashboard(String dashboard) { this.dashboard = dashboard; }
    public int getTransportPort() { return transportPort; }
    public void setTransportPort(int transportPort) { this.transportPort = transportPort; }
    public Nacos getNacos() { return nacos; }

    public static class Nacos {
        private boolean enabled;
        private String serverAddr;
        private String username;
        private String password;
        private String namespace;
        private String group = "DEFAULT_GROUP";
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getServerAddr() { return serverAddr; }
        public void setServerAddr(String value) { this.serverAddr = value; }
        public String getUsername() { return username; }
        public void setUsername(String value) { this.username = value; }
        public String getPassword() { return password; }
        public void setPassword(String value) { this.password = value; }
        public String getNamespace() { return namespace; }
        public void setNamespace(String value) { this.namespace = value; }
        public String getGroup() { return group; }
        public void setGroup(String value) { this.group = value; }
    }
}
