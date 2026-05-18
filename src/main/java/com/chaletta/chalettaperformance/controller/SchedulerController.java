package com.chaletta.chalettaperformance.controller;

import com.chaletta.chalettaperformance.config.SchedulerConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/scheduler")
@RequiredArgsConstructor
public class SchedulerController {

    private final SchedulerConfig schedulerConfig;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getStatus() {
        return ResponseEntity.ok(Map.of(
                "enabled",    schedulerConfig.isEnabled(),
                "intervalMs", schedulerConfig.getIntervalMs()
        ));
    }

    @PostMapping("/start")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> start() {
        schedulerConfig.setEnabled(true);
        return ResponseEntity.ok(Map.of(
                "message", "Scheduler started",
                "enabled", true
        ));
    }

    @PostMapping("/stop")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> stop() {
        schedulerConfig.setEnabled(false);
        return ResponseEntity.ok(Map.of(
                "message", "Scheduler stopped",
                "enabled", false
        ));
    }

    @PostMapping("/interval")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> setInterval(@RequestParam long ms) {
        if (ms < 5000) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Minimum interval is 5000ms (5 seconds)"
            ));
        }
        schedulerConfig.setIntervalMs(ms);
        return ResponseEntity.ok(Map.of(
                "message",    "Interval updated",
                "intervalMs", ms
        ));
    }
}
