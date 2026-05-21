package com.chaletta.chalettaperformance.service.external;

import com.chaletta.chalettaperformance.model.Match;
import com.chaletta.chalettaperformance.model.MatchPlayer;
import com.chaletta.chalettaperformance.model.Player;
import com.chaletta.chalettaperformance.repository.MatchPlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameSyncService {

    private static final int MIN_DURATION_SECONDS = 1200; // 20 minutes

    private final ExternalApiClient           apiClient;
    private final MatchIngestionService       matchIngestionService;
    private final PlayerIngestionService      playerIngestionService;
    private final MatchPlayerIngestionService matchPlayerIngestionService;
    private final MatchPlayerRepository       matchPlayerRepository;

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
                    long duration = info.get("length").asLong();
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

                    // Fix any zero ratingChanges using team context
                    fixZeroRatingChanges(match);
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

    // ─── Rating Change Fix ────────────────────────────────────────────────────

    /**
     * Two-level fix for missing ratingChanges caused by API inconsistencies:
     *
     * Level 1 — Some players on a team have ratingChange = 0, teammates don't:
     *   → Apply the teammates' consistent value to the zero players.
     *
     * Level 2 — ALL players on a team have ratingChange = 0:
     *   → Check the OTHER team's ratingChange.
     *   → Other team got +5 (won) → we lost → apply -3 to entire team.
     *   → Other team got -3 (lost) → we won → apply +5 to entire team.
     */
    private void fixZeroRatingChanges(Match match) {
        List<MatchPlayer> allPlayers = matchPlayerRepository.findByMatch_GameId(match.getGameId());

        // Group players by team number (0 = sentinel, 1 = scourge)
        Map<Integer, List<MatchPlayer>> byTeam = allPlayers.stream()
                .filter(mp -> mp.getTeamNumber() != null)
                .collect(Collectors.groupingBy(MatchPlayer::getTeamNumber));

        if (byTeam.size() != 2) {
            log.debug("Game {} does not have exactly 2 teams, skipping fix.", match.getGameId());
            return;
        }

        List<Integer> keys  = new ArrayList<>(byTeam.keySet());
        List<MatchPlayer> teamA = byTeam.get(keys.get(0));
        List<MatchPlayer> teamB = byTeam.get(keys.get(1));

        fixTeam(match, teamA, teamB);
        fixTeam(match, teamB, teamA);
    }

    private void fixTeam(Match match, List<MatchPlayer> team, List<MatchPlayer> otherTeam) {
        List<MatchPlayer> zeroPlayers = team.stream()
                .filter(mp -> mp.getRatingChange() != null && mp.getRatingChange() == 0)
                .collect(Collectors.toList());

        if (zeroPlayers.isEmpty()) return;

        // ── Level 1: some teammates have a consistent non-zero value ──────────
        List<Integer> nonZeroTeamRatings = team.stream()
                .filter(mp -> mp.getRatingChange() != null && mp.getRatingChange() != 0)
                .map(MatchPlayer::getRatingChange)
                .distinct()
                .collect(Collectors.toList());

        if (nonZeroTeamRatings.size() == 1) {
            applyRatingChange(zeroPlayers, nonZeroTeamRatings.get(0), match.getGameId());
            return;
        }

        // ── Level 2: entire team is 0 — infer from the other team ────────────
        boolean allZero = team.stream()
                .allMatch(mp -> mp.getRatingChange() != null && mp.getRatingChange() == 0);

        if (!allZero) {
            log.debug("Game {} team has mixed ratingChanges, cannot safely fix.", match.getGameId());
            return;
        }

        List<Integer> otherNonZeroRatings = otherTeam.stream()
                .filter(mp -> mp.getRatingChange() != null && mp.getRatingChange() != 0)
                .map(MatchPlayer::getRatingChange)
                .distinct()
                .collect(Collectors.toList());

        if (otherNonZeroRatings.size() == 1) {
            int otherRating = otherNonZeroRatings.get(0);
            // Other team won (+5) → we lost (-3), and vice versa
            int ourRating = otherRating > 0 ? -3 : 5;
            applyRatingChange(team, ourRating, match.getGameId()); // ← entire team
        } else {
            log.debug("Game {} other team has mixed or all-zero ratingChanges, cannot infer result.",
                    match.getGameId());
        }
    }

    private void applyRatingChange(List<MatchPlayer> players, int ratingChange, Long gameId) {
        log.info("Fixing ratingChange → {} for {} player(s) in game {}",
                ratingChange, players.size(), gameId);
        for (MatchPlayer mp : players) {
            mp.setRatingChange(ratingChange);
            matchPlayerRepository.save(mp);
        }
    }
}