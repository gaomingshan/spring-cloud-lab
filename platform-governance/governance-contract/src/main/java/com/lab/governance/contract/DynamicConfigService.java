package com.lab.governance.contract;

public interface DynamicConfigService {
    String get(String dataId, String group);

    <T> T get(String dataId, String group, Class<T> type);

    void addListener(String dataId, String group, ConfigChangeListener listener);
}
