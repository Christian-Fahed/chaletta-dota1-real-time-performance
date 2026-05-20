package com.chaletta.chalettaperformance.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "match_players")
public class MatchPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Match match;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "hero_name")
    private String heroName;

    @Column(name = "hero_class")
    private String heroClass;

    @Column(name = "team_side")
    private Integer teamSide;

    @Column(name = "creep_kills")
    private Integer creepKills;

    @Column(name = "creep_denies")
    private Integer creepDenies;

    @Column(name = "neutral_kills")
    private Integer neutralKills;

    @Column(name = "rating_change")
    private Integer ratingChange;

    @Column(name = "team_number")
    private Integer teamNumber;

    @Column(name = "player_status")
    private Integer playerStatus; // 1 = won, 2 = lost, 5 = abandoned

    private Integer kills;
    private Integer deaths;
    private Integer assists;
}
