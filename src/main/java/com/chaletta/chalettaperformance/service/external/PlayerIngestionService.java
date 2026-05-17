package com.chaletta.chalettaperformance.service.external;

import com.chaletta.chalettaperformance.model.Player;
import com.chaletta.chalettaperformance.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Iterator;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerIngestionService {

    private final PlayerRepository playerRepository;

    /**
     * Check if the specified players node contains
     * any of the players in the database.
     * @param playersNode The specified player node.
     * @return True if it has known players, false otherwise.
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
     * Resolve a player from the Json Node.
     * @param playersNode The specified json node.
     * @return The resolved player if found, null otherwise.
     */
    public Player resolvePlayer(JsonNode playersNode) {
        String uid = playersNode.get("uid").asText();
        return playerRepository.findByUuid(uid).orElse(null);
    }

}
