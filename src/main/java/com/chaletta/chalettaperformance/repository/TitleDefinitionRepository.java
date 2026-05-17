package com.chaletta.chalettaperformance.repository;

import com.chaletta.chalettaperformance.model.TitleDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * <h1>TitleDefinitionRepository</h1>
 *
 * <p>Title Definition Repository Interface containing all methods
 * for managing title definitions</p>
 */
public interface TitleDefinitionRepository extends JpaRepository<TitleDefinition, Integer> {

    /**
     * Find title definition by the specified title name.
     * @param titleName The specified title name.
     * @return The title definition associated with the specified title name, null otherwise.
     */
    Optional<TitleDefinition> findByTitleName(String titleName);
}
