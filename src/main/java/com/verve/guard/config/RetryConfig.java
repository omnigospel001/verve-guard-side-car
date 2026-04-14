package com.verve.guard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class RetryConfig {

    @Value("${retry.max-attempts:4}")
    private int maxAttempts;

    @Value("${retry.initial-interval-ms:500}")
    private long initialInterval;

    @Value("${retry.multiplier:2.0}")
    private double multiplier;

    @Bean
    public RetryTemplate retryTemplate() {

        RetryTemplate template = new RetryTemplate();

        //BackOff Configs
        ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
        backOff.setInitialInterval(initialInterval);
        backOff.setMultiplier(multiplier);
        template.setBackOffPolicy(backOff);

        //Retry attempts Configs
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(maxAttempts);
        template.setRetryPolicy(retryPolicy);

        return template;
    }
}
