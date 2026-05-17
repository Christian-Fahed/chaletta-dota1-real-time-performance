package com.chaletta.chalettaperformance.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "title_definitions")
public class TitleDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "title_name", nullable = false)
    private String titleName;

    @Column(nullable = false)
    private String metric;

    @Column(nullable = false)
    private String aggregation;

    @Column(name = "min_games")
    private Integer minGames = 0;

    private String description;
}
