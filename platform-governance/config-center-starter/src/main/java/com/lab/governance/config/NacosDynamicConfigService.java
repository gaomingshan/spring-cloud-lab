package com.lab.governance.config;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.lab.governance.contract.ConfigChangeListener;
import com.lab.governance.contract.DynamicConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NacosDynamicConfigService implements DynamicConfigService {
    private final ConfigService configService;
    private final ObjectMapper objectMapper;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public NacosDynamicConfigService(ObjectMapper objectMapper) {
        this.configService = NacosConfigManager.getInstance().getConfigService();
        this.objectMapper = objectMapper;
    }

    @Override
    public String get(String dataId, String group) {
        try {
            var value = configService.getConfig(dataId, group, 3_000L);
            if (value != null) cache.put(key(dataId, group), value);
            return value != null ? value : cache.get(key(dataId, group));
        } catch (Exception ex) {
            var cached = cache.get(key(dataId, group));
            if (cached != null) return cached;
            throw new IllegalStateException("Unable to read Nacos config " + dataId + "/" + group, ex);
        }
    }

    @Override
    public <T> T get(String dataId, String group, Class<T> type) {
        try {
            return objectMapper.readValue(get(dataId, group), type);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to decode Nacos config " + dataId + "/" + group, ex);
        }
    }

    @Override
    public void addListener(String dataId, String group, ConfigChangeListener listener) {
        try {
            configService.addListener(dataId, group, new com.alibaba.nacos.api.config.listener.Listener() {
                @Override public java.util.concurrent.Executor getExecutor() { return null; }
                @Override public void receiveConfigInfo(String configInfo) {
                    cache.put(key(dataId, group), configInfo);
                    listener.onChange(configInfo);
                }
            });
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to listen to Nacos config " + dataId + "/" + group, ex);
        }
    }

    private String key(String dataId, String group) { return group + ":" + dataId; }
}
