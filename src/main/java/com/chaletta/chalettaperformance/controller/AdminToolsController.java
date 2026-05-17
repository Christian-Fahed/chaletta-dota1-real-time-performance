package com.chaletta.chalettaperformance.controller;

import com.chaletta.chalettaperformance.service.WeeklyTitleAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin/tools")
@RequiredArgsConstructor
public class AdminToolsController {
    private final WeeklyTitleAssignmentService weeklyTitleAssignmentService;

    /**
     * Assign titles to speciefic week.
     * @param weekStart The week start date.
     * @return ResponseEntity.
     */
    @PostMapping("/assign-titles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignTitles(
            @RequestParam String weekStart) {
        weeklyTitleAssignmentService.assignWeeklyTitlesForWeek(LocalDate.parse(weekStart));
        return ResponseEntity.ok("Titles assigned for week starting " + weekStart);
    }
}
