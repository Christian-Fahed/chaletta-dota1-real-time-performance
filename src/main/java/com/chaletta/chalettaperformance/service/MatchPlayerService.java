package com.chaletta.chalettaperformance.service;


import com.chaletta.chalettaperformance.model.MatchPlayer;
import com.chaletta.chalettaperformance.repository.MatchPlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchPlayerService {

    private final MatchPlayerRepository matchPlayerRepository;

    /**
     * Get all match players.
     * @return List of all match players.
     */
    public List<MatchPlayer> getAll() {
        return matchPlayerRepository.findAll();
    }

    /**
     * Get match player associated with the specified game id.
     * @param gameId The specified game id.
     * @return List of match players based on the game id.
     */
    public List<MatchPlayer> getByMatchId(Long gameId) {
        return matchPlayerRepository.findByMatch_GameId(gameId);
    }

    /**
     * Get match player associated with the specified player id.
     * @param playerId The specified player id.
     * @return List of match players associated with the player id.
     */
    public List<MatchPlayer> getByPlayerId(Long playerId) {
        return matchPlayerRepository.findByPlayer_PlayerId(playerId);
    }

    /**
     * Save a match player.
     * @param matchPlayer The specified match player to be saved.
     * @return The saved match player.
     */
    public MatchPlayer save(MatchPlayer matchPlayer) {
        return matchPlayerRepository.save(matchPlayer);
    }

    /**
     * Delete a match player.
     * @param id The specified match player id.
     */
    public void delete(Long id) {
        matchPlayerRepository.deleteById(id);
    }

}
