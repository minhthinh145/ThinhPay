package com.thinhpay.backend.shared.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async Configuration - Enable asynchronous processing.
 * Sử dụng cho EmailService và các background tasks.
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    /**
     * Task executor for async methods.
     * Cấu hình thread pool cho @Async methods.
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        log.info("Creating Async Task Executor");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);           // Số threads cơ bản
        executor.setMaxPoolSize(10);           // Số threads tối đa
        executor.setQueueCapacity(100);        // Queue size
        executor.setThreadNamePrefix("async-"); // Thread name prefix
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();

        log.info("Async Task Executor initialized - CorePoolSize: 3, MaxPoolSize: 10");
        return executor;
    }
}
