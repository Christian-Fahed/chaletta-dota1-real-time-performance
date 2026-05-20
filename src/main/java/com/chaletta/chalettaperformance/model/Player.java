package com.chaletta.chalettaperformance.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "player_id")
    private Long playerId;

    @Column(unique = true)
    private String uuid;

    private String username;

    @Column(name = "points", nullable = false)
    private Integer points = 100;
}
