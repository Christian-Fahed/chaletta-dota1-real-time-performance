package com.chaletta.chalettaperformance.repository;

import com.chaletta.chalettaperformance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * <h1>UserRepository</h1>
 *
 * <p>User Repository Interface containing all methods
 * for managing users</p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Find user by the specified username.
     * @param username The specified username.
     * @return User if found, null otherwise.
     */
    Optional<User> findByUsername(String username);
}

