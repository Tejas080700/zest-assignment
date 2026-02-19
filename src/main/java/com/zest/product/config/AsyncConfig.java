package com.zest.product.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // Spring Boot auto-configures a SimpleAsyncTaskExecutor.
    // Customize thread pool here if needed.
}
