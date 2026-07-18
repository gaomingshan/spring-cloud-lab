package com.lab.message.local;

import com.lab.message.contract.MessageException;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lab.message.local")
public class LocalMessageProperties {
    private boolean enabled = true;
    private String dispatchMode = "sync";
    private int coreSize = 2;
    private int maxSize = 8;
    private int queueCapacity = 1000;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getDispatchMode() { return dispatchMode; }
    public void setDispatchMode(String dispatchMode) { this.dispatchMode = dispatchMode; }
    public int getCoreSize() { return coreSize; }
    public void setCoreSize(int coreSize) { this.coreSize = coreSize; }
    public int getMaxSize() { return maxSize; }
    public void setMaxSize(int maxSize) { this.maxSize = maxSize; }
    public int getQueueCapacity() { return queueCapacity; }
    public void setQueueCapacity(int queueCapacity) { this.queueCapacity = queueCapacity; }

    public void validate() {
        if (!"sync".equalsIgnoreCase(dispatchMode) && !"async".equalsIgnoreCase(dispatchMode)) {
            throw new MessageException("CONFIGURATION_FAILED: dispatch-mode must be sync or async");
        }
        if (coreSize <= 0 || maxSize <= 0 || queueCapacity <= 0 || coreSize > maxSize) {
            throw new MessageException("CONFIGURATION_FAILED: executor sizes must be positive and core-size must not exceed max-size");
        }
    }
}
