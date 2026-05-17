package com.chaletta.chalettaperformance.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecentMatchPlayerDto {
    private String username;
    private String heroName;
    private String heroClass;
    private Integer teamSide;
    private Integer kills;
    private Integer deaths;
    private Integer assists;
}
