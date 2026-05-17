package com.chaletta.chalettaperformance.controller;

import com.chaletta.chalettaperformance.dto.stats.*;
import com.chaletta.chalettaperformance.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @GetMapping("/overall")
    public ResponseEntity<List<PlayerOverallStatsDto>> getOverall() {
        return ResponseEntity.ok(statsService.getOverallStats());
    }

    @GetMapping("/weekly")
    public ResponseEntity<List<PlayerWeeklyStatsDto>> getWeekly(
            @RequestParam Long from,
            @RequestParam Long to) {
        return ResponseEntity.ok(statsService.getWeeklyStats(from, to));
    }

    @GetMapping("/heroes")
    public ResponseEntity<List<HeroPlayerStatsDto>> getHeroStats() {
        return ResponseEntity.ok(statsService.getHeroStats());
    }

    @GetMapping("/leaderboard/kills")
    public ResponseEntity<List<LeaderboardEntryDto>> getKillsLeaderboard() {
        return ResponseEntity.ok(statsService.getKillsLeaderboard());
    }

    @GetMapping("/heroes/player/{username}")
    public ResponseEntity<?> getHeroStatsByPlayer(@PathVariable String username) {
        return ResponseEntity.ok(statsService.getHeroStatsByPlayer(username));
    }
}
