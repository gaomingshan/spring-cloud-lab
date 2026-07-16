package com.lab.foundation.starter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class FoundationAutoConfiguration {

    @Bean
    FoundationExceptionHandler foundationExceptionHandler() {
        return new FoundationExceptionHandler();
    }
}
