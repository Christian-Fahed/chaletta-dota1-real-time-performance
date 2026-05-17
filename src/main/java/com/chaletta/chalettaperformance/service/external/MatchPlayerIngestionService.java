package com.chaletta.chalettaperformance.service.external;

import com.chaletta.chalettaperformance.model.Match;
import com.chaletta.chalettaperformance.model.MatchPlayer;
import com.chaletta.chalettaperformance.model.Player;
import com.chaletta.chalettaperformance.repository.MatchPlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;

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
        JsonNode stats = playerNode.get("stats");
        JsonNode hero  = playerNode.get("hero");

        // skip if hero is not an object (hero: 0 means abandoned/no data)
        if (hero == null || !hero.isObject()) {
            log.info("Skipping match player — no hero data for player {} in game {}",
                    player.getUsername(), match.getGameId());
            return;
        }

        MatchPlayer mp = new MatchPlayer();
        mp.setMatch(match);
        mp.setPlayer(player);
        mp.setTeamSide(playerNode.get("sid").asInt());
        mp.setHeroName(hero.get("name").asText());
        mp.setHeroClass(hero.get("class").asText());
        mp.setKills(stats.get("a").asInt());
        mp.setDeaths(stats.get("b").asInt());
        mp.setAssists(stats.get("c").asInt());

        matchPlayerRepository.save(mp);
    }

}
