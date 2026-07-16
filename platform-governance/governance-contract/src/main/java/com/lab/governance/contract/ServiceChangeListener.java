package com.lab.governance.contract;

import java.util.List;

@FunctionalInterface
public interface ServiceChangeListener {
    void onChange(List<ServiceInstanceView> instances);
}
