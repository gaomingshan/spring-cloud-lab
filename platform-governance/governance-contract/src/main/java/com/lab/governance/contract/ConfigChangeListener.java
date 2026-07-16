package com.lab.governance.contract;

@FunctionalInterface
public interface ConfigChangeListener {
    void onChange(String content);
}
