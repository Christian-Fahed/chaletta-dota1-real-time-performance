package com.chaletta.chalettaperformance.service;

import com.chaletta.chalettaperformance.model.WeeklyTitle;
import com.chaletta.chalettaperformance.repository.WeeklyTitleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeeklyTitleService {
    private final WeeklyTitleRepository weeklyTitleRepository;

    public List<WeeklyTitle> getAll() {
        return weeklyTitleRepository.findAll();
    }

    public List<WeeklyTitle> getByPlayerId(Long playerId) {
        return weeklyTitleRepository.findByPlayer_PlayerId(playerId);
    }

    public List<WeeklyTitle> getByWeekStart(LocalDate weekStart) {
        return weeklyTitleRepository.findByWeekStart(weekStart);
    }

    public WeeklyTitle save(WeeklyTitle weeklyTitle) {
        return weeklyTitleRepository.save(weeklyTitle);
    }

    public void delete(Long id) {
        weeklyTitleRepository.deleteById(id);
    }

}
