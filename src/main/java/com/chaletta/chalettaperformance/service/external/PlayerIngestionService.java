package com.chaletta.chalettaperformance.service.external;

import com.chaletta.chalettaperformance.model.Player;
import com.chaletta.chalettaperformance.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Iterator;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerIngestionService {

    private final PlayerRepository playerRepository;

    /**
     * Check if the specified players node contains
     * any of the players in the database.
     */
    public boolean hasKnownPlayer(JsonNode playersNode) {
        Iterator<JsonNode> iter = playersNode.elements();
        while (iter.hasNext()) {
            String uid = iter.next().get("uid").asText();
            if (playerRepository.findByUuid(uid).isPresent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resolve a registered player from the JSON node.
     * Points are NOT updated here — they are computed dynamically
     * from SUM(ratingChange) in StatsService.
     *
     * @return The player if registered, null if unknown.
     */
    public Player resolvePlayer(JsonNode playerNode) {
        String uid = playerNode.get("uid").asText();

        Optional<Player> existing = playerRepository.findByUuid(uid);
        return existing.orElse(null);
    }
}