package com.chaletta.chalettaperformance.controller;

import com.chaletta.chalettaperformance.model.Match;
import com.chaletta.chalettaperformance.model.MatchPlayer;
import com.chaletta.chalettaperformance.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService  matchService;

    /**
     * Get all matches endpoint.
     * @return all matches.
     */
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to) {

        if (from != null && to != null) {
            return ResponseEntity.ok(matchService.getByTimeRange(from, to, page, size));
        }
        return ResponseEntity.ok(matchService.getAll(page, size));
    }

    /**
     * Get match by id.
     * @param id The specified map id.
     * @return Match.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Match> getById(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getById(id));
    }

    /**
     * Delete match with the specified id.
     * @param id The specified id.
     * @return Void.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        matchService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
