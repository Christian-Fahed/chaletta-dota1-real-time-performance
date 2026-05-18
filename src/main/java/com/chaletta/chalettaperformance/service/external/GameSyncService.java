package com.chaletta.chalettaperformance.service.external;

import com.chaletta.chalettaperformance.model.Match;
import com.chaletta.chalettaperformance.model.Player;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.time.Year;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameSyncService {

    private static final int MIN_DURATION_SECONDS = 1200; // 20 minutes

    private final ExternalApiClient              apiClient;
    private final MatchIngestionService          matchIngestionService;
    private final PlayerIngestionService         playerIngestionService;
    private final MatchPlayerIngestionService    matchPlayerIngestionService;

    /**
     * Sync games with the RGC API.
     * Only stores games that meet ALL of the following:
     * - Played in the current year
     * - Duration >= 20 minutes (1200 seconds)
     * - At least 6 players
     * - At least one registered player
     * Stops when a known game or a game from a previous year is encountered.
     */
    public void sync() {
        try {
            int     currentPage = 1;
            boolean stopSync    = false;
            int     currentYear = Year.now().getValue();

            while (!stopSync) {
                JsonNode root     = apiClient.fetchGames(currentPage);
                JsonNode games    = root.get("games");
                JsonNode pageInfo = root.get("info");

                if (games == null || !games.isArray()) {
                    log.warn("No games returned on page {}", currentPage);
                    break;
                }

                int totalPages = pageInfo.get("pages").asInt();
                log.info("Processing page {}/{}", currentPage, totalPages);

                for (JsonNode gameNode : games) {
                    JsonNode info    = gameNode.get("info");
                    JsonNode players = gameNode.get("player");
                    Long     gameId  = info.get("ID").asLong();

                    // Stop if game already exists in DB
                    if (matchIngestionService.matchExists(gameId)) {
                        log.info("Game {} already exists, stopping sync.", gameId);
                        stopSync = true;
                        break;
                    }

                    // Stop if game is from a previous year
                    long startedAt = info.get("started").asLong();
                    int  gameYear  = Instant.ofEpochSecond(startedAt)
                            .atZone(ZoneOffset.UTC)
                            .getYear();
                    if (gameYear < currentYear) {
                        log.info("Game {} was played in {}, stopping sync (only storing {}).",
                                gameId, gameYear, currentYear);
                        stopSync = true;
                        break;
                    }

                    // Skip games shorter than 20 minutes
                    long duration = info.get("time").asLong();
                    if (duration < MIN_DURATION_SECONDS) {
                        log.info("Game {} duration {}s is under {} minutes, skipping.",
                                gameId, duration, MIN_DURATION_SECONDS / 60);
                        continue;
                    }

                    // Skip games with less than 6 players
                    if (players == null || players.size() < 6) {
                        log.info("Game {} has less than 6 players, skipping.", gameId);
                        continue;
                    }

                    // Skip games with no known players
                    if (!playerIngestionService.hasKnownPlayer(players)) {
                        log.info("Game {} has no known players, skipping.", gameId);
                        continue;
                    }

                    // Save the match
                    Match match = matchIngestionService.createMatch(gameId, info);
                    log.info("Saved match {} ({}m {}s).",
                            gameId, duration / 60, duration % 60);

                    // Save each known player's match entry
                    players.fields().forEachRemaining(entry -> {
                        JsonNode playerNode = entry.getValue();
                        Player   player     = playerIngestionService.resolvePlayer(playerNode);

                        if (player == null) {
                            log.debug("Unknown player uid={}, skipping.",
                                    playerNode.get("uid").asText());
                            return;
                        }

                        matchPlayerIngestionService.createMatchPlayer(match, player, playerNode);
                    });
                }

                if (currentPage >= totalPages) {
                    log.info("All {} pages processed.", totalPages);
                    stopSync = true;
                }

                currentPage++;
            }

        } catch (Exception e) {
            log.error("Sync failed: {}", e.getMessage());
        }
    }
}