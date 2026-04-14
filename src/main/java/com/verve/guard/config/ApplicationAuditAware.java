package com.verve.guard.config;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class ApplicationAuditAware implements AuditorAware<Long>{

    @Override
    @NullMarked
    public Optional<Long> getCurrentAuditor() {
        return Optional.of(1L);
    }
}
