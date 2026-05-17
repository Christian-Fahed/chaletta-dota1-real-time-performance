package com.chaletta.chalettaperformance.controller;

import com.chaletta.chalettaperformance.model.MatchPlayer;
import com.chaletta.chalettaperformance.service.MatchPlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/match-players")
@RequiredArgsConstructor
public class MatchPlayerController {

    private final MatchPlayerService  matchPlayerService;

    @GetMapping("/match/{gameId}")
    public ResponseEntity<List<MatchPlayer>> getByMatchId(@PathVariable Long gameId) {
        return ResponseEntity.ok(matchPlayerService.getByMatchId(gameId));
    }

    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<MatchPlayer>> getByPlayerId(@PathVariable Long playerId) {
        return ResponseEntity.ok(matchPlayerService.getByPlayerId(playerId));
    }

    /**
     * Delete match with the specified id.
     * @param id The specified match id.
     * @return Void.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        matchPlayerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
