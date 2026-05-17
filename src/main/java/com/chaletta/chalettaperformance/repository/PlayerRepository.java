package com.chaletta.chalettaperformance.repository;

import com.chaletta.chalettaperformance.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * <h1>PlayerRepository</h1>
 *
 * <p>Player Repository Interface containing all methods
 * for managing players</p>
 */
@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    /**
     * Find user by uuid.
     * @param uuid The specified uuid.
     * @return The player associated with the uuid, null otherwise.
     */
    Optional<Player> findByUuid(String uuid);

    /**
     * Find player by username.
     * @param username The specified username.
     * @return User associated with the username, null otherwise.
     */
    Optional<Player> findByUsername(String username);

    @Query("SELECT COUNT(p) FROM Player p")
    Long totalPlayers();
}
