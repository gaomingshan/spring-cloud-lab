package com.lab.governance.discovery;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.lab.governance.contract.ServiceChangeListener;
import com.lab.governance.contract.ServiceDiscovery;
import com.lab.governance.contract.ServiceInstanceView;

import java.util.List;
import java.util.Optional;

public class NacosServiceDiscovery implements ServiceDiscovery {
    private final NamingService namingService;
    private final ServiceDiscoveryProperties properties;

    public NacosServiceDiscovery(NacosDiscoveryProperties nacosProperties, ServiceDiscoveryProperties properties) {
        this.namingService = nacosProperties.namingServiceInstance();
        this.properties = properties;
    }

    @Override
    public List<ServiceInstanceView> getInstances(String serviceId) {
        try {
            return namingService.getAllInstances(serviceId, properties.getGroup(), List.of(properties.getCluster()), true)
                    .stream().map(this::view).toList();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to discover service " + serviceId, ex);
        }
    }

    @Override
    public Optional<ServiceInstanceView> choose(String serviceId) {
        return getInstances(serviceId).stream().findFirst();
    }

    @Override
    public void addListener(String serviceId, ServiceChangeListener listener) {
        try {
            namingService.subscribe(serviceId, properties.getGroup(), event -> listener.onChange(getInstances(serviceId)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to listen to service " + serviceId, ex);
        }
    }

    private ServiceInstanceView view(Instance instance) {
        return new ServiceInstanceView(instance.getServiceName(), instance.getIp(), instance.getPort(),
                instance.isHealthy(), instance.getWeight(), instance.getClusterName(), instance.getClusterName());
    }
}
