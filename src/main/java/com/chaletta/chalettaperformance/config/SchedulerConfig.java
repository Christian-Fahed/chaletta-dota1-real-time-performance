package com.chaletta.chalettaperformance.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix = "app.scheduler")
@Getter
@Setter
public class SchedulerConfig {
    private long intervalMs;
    private boolean enabled;
}
