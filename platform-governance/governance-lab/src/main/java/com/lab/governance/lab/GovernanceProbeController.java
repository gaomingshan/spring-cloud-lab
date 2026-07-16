package com.lab.governance.lab;

import com.lab.foundation.context.RequestContextHolder;
import com.lab.foundation.contract.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GovernanceProbeController {

    private final AppProperties appProperties;

    public GovernanceProbeController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @GetMapping("/governance/probe")
    public ApiResponse<AppProperties> probe() {
        var context = RequestContextHolder.get();
        var traceId = context == null ? null : context.traceId();
        return ApiResponse.success(appProperties, traceId);
    }
}
