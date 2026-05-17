package com.chaletta.chalettaperformance.service;

import com.chaletta.chalettaperformance.model.Match;
import com.chaletta.chalettaperformance.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchService {


    private final MatchRepository matchRepository;

    public Page<Match> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startedAt").descending());
        return matchRepository.findAll(pageable);
    }

    public Page<Match> getByTimeRange(Long from, Long to, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startedAt").descending());
        return matchRepository.findByStartedAtBetween(from, to, pageable);
    }

    public Match getById(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Match not found"));
    }

    public void delete(Long id) {
        matchRepository.deleteById(id);
    }
}
