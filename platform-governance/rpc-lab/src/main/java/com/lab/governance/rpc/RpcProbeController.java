package com.lab.governance.rpc;

import com.lab.foundation.contract.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RpcProbeController {

    private final GovernanceClient governanceClient;

    public RpcProbeController(GovernanceClient governanceClient) {
        this.governanceClient = governanceClient;
    }

    @GetMapping("/rpc/probe")
    public ApiResponse<String> probe() {
        return ApiResponse.success(governanceClient.probe(), null);
    }
}
