package com.verve.guard.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class KafkaTopic {

    @Bean
    public NewTopic VerveGuardTopic() {
        return TopicBuilder
                .name("verve-guard-topic")
                .build();
    }

    @Bean
    public NewTopic TransferTopic() {
        return TopicBuilder
                .name("transfer-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic withdrawalTopic() {
        return TopicBuilder
                .name("withdrawal-topic")
                .build();
    }

    @Bean
    public NewTopic depositTopic() {
        return TopicBuilder
                .name("deposit-topic")
                .build();
    }


    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("DynamicTransfer-");
        scheduler.initialize();
        return scheduler;
    }
}
