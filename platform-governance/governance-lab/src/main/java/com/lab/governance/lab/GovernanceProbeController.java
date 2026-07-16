package com.lab.governance.lab;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GovernanceProbeController {

    private final AppProperties appProperties;

    public GovernanceProbeController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @GetMapping("/governance/probe")
    public AppProperties probe() {
        return appProperties;
    }
}
