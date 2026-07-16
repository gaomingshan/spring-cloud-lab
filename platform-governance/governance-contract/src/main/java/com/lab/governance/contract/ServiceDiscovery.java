package com.lab.governance.contract;

import java.util.List;
import java.util.Optional;

public interface ServiceDiscovery {
    List<ServiceInstanceView> getInstances(String serviceId);

    Optional<ServiceInstanceView> choose(String serviceId);

    void addListener(String serviceId, ServiceChangeListener listener);
}
