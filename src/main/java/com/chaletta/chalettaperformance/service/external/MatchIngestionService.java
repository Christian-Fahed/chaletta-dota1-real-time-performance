package com.chaletta.chalettaperformance.service.external;

import com.chaletta.chalettaperformance.model.Match;
import com.chaletta.chalettaperformance.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchIngestionService {

    private final MatchRepository matchRepository;

    /**
     * Create a match and return it based on the Json Node info specified.
     * @param info The Json Info data specified.
     * @return The match created based on Json Node info.
     */
    public Match createMatch(Long gameId, JsonNode info) {
        Match match = new Match();
        match.setGameId(gameId);
        match.setStartedAt(info.get("started").asLong());
        match.setDuration(info.get("length").asInt());
        match.setTeamWinnerSide(info.get("winner").asInt());
        match.setStatus(info.get("scored").asInt());
        return matchRepository.save(match);
    }

    /**
     * Check if the specified game id already exists in the database.
     * @param gameId The specified game id.
     * @return True if it exists, false otherwise.
     */
    public boolean matchExists(Long gameId) {
        return matchRepository.existsById(gameId);
    }

}
