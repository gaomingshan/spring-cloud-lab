package com.lab.commerce.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "seata-account", path = "/account")
public interface AccountClient {

    @PostMapping("/decrease")
    void decrease(@RequestParam Long userId, @RequestParam BigDecimal money);
}
