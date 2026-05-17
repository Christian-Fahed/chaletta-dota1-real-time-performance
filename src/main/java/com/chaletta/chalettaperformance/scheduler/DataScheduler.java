package com.chaletta.chalettaperformance.scheduler;

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

    @Scheduled(fixedRate = 15000)
    public void fetchMatches() {
        log.info("Running game sync...");
        gameSyncService.sync();
    }

    // Every Sunday at midnight
    @Scheduled(cron = "0 0 0 * * SUN")
    public void assignWeeklyTitles() {
        log.info("Running weekly title assignments...");
        weeklyTitleAssignmentService.assignWeeklyTitles();
    }

}
