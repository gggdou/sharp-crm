package com.sharpcrm.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池配置：异步处理操作日志等耗时任务
 */
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    /** 核心线程数 = CPU 核数，适合 IO 密集型任务 */
    private static final int CORE_SIZE = Runtime.getRuntime().availableProcessors();

    @Bean("asyncExecutor")
    public Executor asyncExecutor() {
        ThreadFactory factory = new ThreadFactory() {
            private final AtomicInteger index = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "sharp-async-" + index.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
        };

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CORE_SIZE,
                CORE_SIZE * 2,
                60L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(500),
                factory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }
}
