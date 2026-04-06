package com.lab.config.controller;

import com.lab.common.result.Result;
import com.lab.config.properties.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 配置中心演示 Controller
 *
 * <p>重点：@RefreshScope 使 Bean 在 Nacos 配置变更后自动刷新
 * 触发刷新方式：
 *   1. POST <a href="http://localhost:8081/actuator/refresh">...</a>（手动）
 *   2. Nacos 控制台修改配置后自动推送（自动）
 */
@Slf4j
@RefreshScope  // 关键注解：使该 Bean 感知 Nacos 配置变化并自动重建
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class ConfigDemoController {

    /**
     * &#064;Value  注入的属性在 @RefreshScope 下会动态刷新
     * 在 Nacos 控制台修改 app.name 后，此处值会自动更新
     */
    @Value("${app.name:默认应用名}")
    private String appName;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.description:默认描述}")
    private String appDescription;

    /** &#064;ConfigurationProperties  方式注入，同样支持动态刷新 */
    private final AppProperties appProperties;

    /**
     * 查看当前生效的配置
     * 修改 Nacos 配置后，刷新此接口观察配置是否更新
     */
    @GetMapping("/show")
    public Result<Map<String, Object>> showConfig() {
        log.info("[Config] 获取当前配置：appName={}, version={}", appName, appVersion);
        return Result.ok(Map.of(
                "appName", appName,
                "appVersion", appVersion,
                "appDescription", appDescription,
                "propertiesBean", Map.of(
                        "name", appProperties.getName(),
                        "version", appProperties.getVersion()
                )
        ));
    }
}
