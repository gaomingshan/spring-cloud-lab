package com.lab.governance.lab;

import com.lab.foundation.context.RequestContextHolder;
import com.lab.foundation.contract.ApiResponse;
import com.lab.governance.contract.DynamicConfigService;
import com.lab.governance.contract.ServiceDiscovery;
import com.lab.governance.contract.ServiceInstanceView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GovernanceProbeController {

    private final AppProperties appProperties;
    private final DynamicConfigService dynamicConfigService;
    private final ServiceDiscovery serviceDiscovery;

    @GetMapping("/governance/probe")
    public ApiResponse<AppProperties> probe() {
        var context = RequestContextHolder.get();
        var traceId = context == null ? null : context.traceId();
        return ApiResponse.success(appProperties, traceId);
    }

    @GetMapping(value = "/governance/config/{dataId}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String config(@PathVariable String dataId) {
        return dynamicConfigService.get(dataId, "DEFAULT_GROUP");
    }

    @GetMapping("/governance/services/{serviceId}")
    public List<ServiceInstanceView> services(@PathVariable String serviceId) {
        return serviceDiscovery.getInstances(serviceId);
    }
}
