package com.chaletta.chalettaperformance.service.external;

import com.chaletta.chalettaperformance.model.Match;
import com.chaletta.chalettaperformance.model.MatchPlayer;
import com.chaletta.chalettaperformance.model.Player;
import com.chaletta.chalettaperformance.repository.MatchPlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchPlayerIngestionService {

    private final MatchPlayerRepository matchPlayerRepository;

    /**
     * Create a match player.
     * @param match The specified match.
     * @param player The specified player.
     * @param playerNode The player node from where to fetch the necessary info.
     */
    public void createMatchPlayer(Match match, Player player, JsonNode playerNode) {
        Optional<MatchPlayer> existing = matchPlayerRepository
                .findByMatch_GameIdAndPlayer_PlayerId(match.getGameId(), player.getPlayerId());
        if (existing.isPresent()) return;

        JsonNode stats = playerNode.get("stats");

        MatchPlayer mp = new MatchPlayer();
        mp.setMatch(match);
        mp.setPlayer(player);
        mp.setTeamSide(playerNode.get("sid").asInt());
        mp.setTeamNumber(playerNode.get("sid").asInt() <= 4 ? 0 : 1);
        mp.setPlayerStatus(playerNode.get("status").asInt());
        mp.setRatingChange(playerNode.get("ratingchange").asInt());

        // Hero
        JsonNode heroNode = playerNode.get("hero");
        if (heroNode != null && heroNode.isObject()) {
            mp.setHeroName(heroNode.get("name").asText(null));
            mp.setHeroClass(heroNode.get("class").asText(null));
        }

        // KDA
        mp.setKills(stats != null && stats.has("a") ? stats.get("a").asInt() : 0);
        mp.setDeaths(stats != null && stats.has("b") ? stats.get("b").asInt() : 0);
        mp.setAssists(stats != null && stats.has("c") ? stats.get("c").asInt() : 0);

        // Creep stats
        mp.setCreepKills(stats != null && stats.has("d") ? stats.get("d").asInt() : 0);
        mp.setCreepDenies(stats != null && stats.has("e") ? stats.get("e").asInt() : 0);
        mp.setNeutralKills(stats != null && stats.has("f") ? stats.get("f").asInt() : 0);

        matchPlayerRepository.save(mp);
    }

}
