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

    @Column(name = "team_number")
    private Integer teamNumber;

    private Integer kills;
    private Integer deaths;
    private Integer assists;
}
