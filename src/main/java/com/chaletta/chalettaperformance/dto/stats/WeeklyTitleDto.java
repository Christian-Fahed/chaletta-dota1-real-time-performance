package com.chaletta.chalettaperformance.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WeeklyTitleDto {
    private String titleName;
    private String playerUsername;
    private Double value;
}
