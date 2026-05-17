package com.chaletta.chalettaperformance.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RecentMatchDto {
    private Long gameId;
    private Long startedAt;
    private Integer duration;
    private Integer teamWinnerSide;
    private List<RecentMatchPlayerDto> players;
}
