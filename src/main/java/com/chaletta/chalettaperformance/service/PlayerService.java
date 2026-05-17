package com.chaletta.chalettaperformance.service;

import com.chaletta.chalettaperformance.model.Player;
import com.chaletta.chalettaperformance.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

    /**
     * Get all players.
     *
     * @return List of all players.
     */
    public List<Player> getAll() {
        return playerRepository.findAll();
    }

    public Player getById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Player not found"));
    }

    public Player create(Player player) {
        return playerRepository.save(player);
    }

    public Player update(Long id, Player updated) {
        Player existing = getById(id);
        existing.setUuid(updated.getUuid());
        existing.setUsername(updated.getUsername());
        return playerRepository.save(existing);
    }

    public void delete(Long id) {
        playerRepository.deleteById(id);
    }

}
