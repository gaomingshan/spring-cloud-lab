package com.lab.seata.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "seata-storage", path = "/storage")
public interface StorageClient {
    @PostMapping("/decrease")
    void decrease(@RequestParam Long productId, @RequestParam Integer count);
}
