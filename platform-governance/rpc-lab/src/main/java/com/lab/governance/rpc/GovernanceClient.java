package com.lab.governance.rpc;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "governance-lab")
public interface GovernanceClient {

    @GetMapping("/governance/probe")
    String probe();
}
