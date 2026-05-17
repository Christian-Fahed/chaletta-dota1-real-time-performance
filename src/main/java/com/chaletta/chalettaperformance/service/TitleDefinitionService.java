package com.chaletta.chalettaperformance.service;

import com.chaletta.chalettaperformance.model.TitleDefinition;
import com.chaletta.chalettaperformance.repository.TitleDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TitleDefinitionService {
    private final TitleDefinitionRepository titleDefinitionRepository;

    public List<TitleDefinition> getAll() {
        return titleDefinitionRepository.findAll();
    }

    public TitleDefinition getById(Integer id) {
        return titleDefinitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Title definition not found"));
    }

    public TitleDefinition update(Integer id, TitleDefinition updated) {
        TitleDefinition existing = getById(id);
        existing.setTitleName(updated.getTitleName());
        existing.setMetric(updated.getMetric());
        existing.setAggregation(updated.getAggregation());
        existing.setMinGames(updated.getMinGames());
        existing.setDescription(updated.getDescription());
        return titleDefinitionRepository.save(existing);
    }

    public TitleDefinition save(TitleDefinition titleDefinition) {
        return titleDefinitionRepository.save(titleDefinition);
    }

    public void delete(Integer id) {
        titleDefinitionRepository.deleteById(id);
    }
}
