package com.chaletta.chalettaperformance.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "weekly_titles",
        uniqueConstraints = @UniqueConstraint(columnNames = {"player_id", "week_start", "title_name"}))

public class WeeklyTitle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @Column(name = "title_name", nullable = false)
    private String titleName;

    private Double value;
}
