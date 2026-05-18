package com.chaletta.chalettaperformance.scheduler;

import com.chaletta.chalettaperformance.config.SchedulerConfig;
import com.chaletta.chalettaperformance.service.WeeklyTitleAssignmentService;
import com.chaletta.chalettaperformance.service.external.GameSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataScheduler {

    private final GameSyncService gameSyncService;
    private final WeeklyTitleAssignmentService weeklyTitleAssignmentService;
    private final SchedulerConfig schedulerConfig;

    private long lastRun = 0;

    @Scheduled(fixedDelayString = "1000") // checks every second
    public void fetchMatches() {
        if (!schedulerConfig.isEnabled()) return;

        long now = System.currentTimeMillis();
        if (now - lastRun < schedulerConfig.getIntervalMs()) return;

        lastRun = now;
        log.info("Running game sync... (interval: {}ms)", schedulerConfig.getIntervalMs());
        gameSyncService.sync();
    }


    // Every Sunday at midnight
    @Scheduled(cron = "0 0 0 * * SUN")
    public void assignWeeklyTitles() {
        log.info("Running weekly title assignments...");
        weeklyTitleAssignmentService.assignWeeklyTitles();
    }

}
