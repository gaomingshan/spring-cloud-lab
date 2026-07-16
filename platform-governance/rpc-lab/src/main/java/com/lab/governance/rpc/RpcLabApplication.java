package com.lab.governance.rpc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class RpcLabApplication {
    public static void main(String[] args) {
        SpringApplication.run(RpcLabApplication.class, args);
    }
}
