package com.chaletta.chalettaperformance.controller;

import com.chaletta.chalettaperformance.model.WeeklyTitle;
import com.chaletta.chalettaperformance.repository.WeeklyTitleRepository;
import com.chaletta.chalettaperformance.service.WeeklyTitleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/weekly-titles")
@RequiredArgsConstructor
public class WeeklyTitleController {

    private final WeeklyTitleService weeklyTitleService;
    private final WeeklyTitleRepository weeklyTitleRepository;


    @GetMapping
    public ResponseEntity<List<WeeklyTitle>> getAll() {
        return ResponseEntity.ok(weeklyTitleService.getAll());
    }

    @GetMapping("/range")
    public ResponseEntity<List<WeeklyTitle>> getByRange(
            @RequestParam String from,
            @RequestParam String to) {
        LocalDate fromDate = LocalDate.parse(from);
        LocalDate toDate   = LocalDate.parse(to);
        return ResponseEntity.ok(weeklyTitleRepository.findByWeekStartBetween(fromDate, toDate));
    }

    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<WeeklyTitle>> getByPlayerId(@PathVariable Long playerId) {
        return ResponseEntity.ok(weeklyTitleService.getByPlayerId(playerId));
    }

    @GetMapping("/week/{weekStart}")
    public ResponseEntity<List<WeeklyTitle>> getByWeekStart(@PathVariable LocalDate weekStart) {
        return ResponseEntity.ok(weeklyTitleService.getByWeekStart(weekStart));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        weeklyTitleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
