package com.lab.registry.consumer.controller;

import com.lab.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/consumer")
@RequiredArgsConstructor
public class ConsumerController {

    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;

    private static final String PROVIDER_SERVICE = "lab-gateway/registry-provider";

    /**
     * 通过 @LoadBalanced RestTemplate 调用 provider
     * 使用服务名替代 IP:Port，LoadBalancer 自动选择实例
     * 多次调用观察返回的 instancePort 变化（默认轮询策略）
     */
    @GetMapping("/call")
    public Result<Object> callProvider() {
        String url = "http://" + PROVIDER_SERVICE + "/provider/hello";
        log.info("[Consumer] 调用 provider，URL: {}", url);
        Object response = restTemplate.getForObject(url, Object.class);
        return Result.ok(response);
    }

    /**
     * 调用 provider echo 接口
     */
    @GetMapping("/echo/{msg}")
    public Result<String> echo(@PathVariable String msg) {
        String url = "http://" + PROVIDER_SERVICE + "/provider/echo/" + msg;
        return Result.ok(restTemplate.getForObject(url, String.class));
    }

    /**
     * 通过 DiscoveryClient 查看注册中心中的所有服务实例
     * 演示服务发现原理
     */
    @GetMapping("/instances")
    public Result<Map<String, Object>> listInstances() {
        List<ServiceInstance> instances = discoveryClient.getInstances(PROVIDER_SERVICE);
        List<Map<String, Object>> instanceInfos = instances.stream()
                .map(i -> Map.of(
                        "instanceId", i.getInstanceId(),
                        "host", i.getHost(),
                        "port", i.getPort(),
                        "metadata", i.getMetadata()
                ))
                .toList();
        return Result.ok(Map.of(
                "serviceName", PROVIDER_SERVICE,
                "instanceCount", instances.size(),
                "instances", instanceInfos
        ));
    }

    /**
     * 查看所有注册的服务列表
     */
    @GetMapping("/services")
    public Result<List<String>> listServices() {
        return Result.ok(discoveryClient.getServices());
    }
}
