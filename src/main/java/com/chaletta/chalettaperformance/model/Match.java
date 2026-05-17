package com.chaletta.chalettaperformance.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "matches")
public class Match {

    @Id
    @Column(name = "game_id")
    private Long gameId;

    @Column(name = "started_at")
    private Long startedAt;

    private Integer duration;

    @Column(name = "team_winner_side")
    private Integer teamWinnerSide;

    private Integer status;
}
