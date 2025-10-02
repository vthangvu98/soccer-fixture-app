package com.thangv.SoccerFixturesApp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "fixtures", indexes = {@Index(name = "idx_fixtures_match_utc", columnList = "match_utc")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fixture {

    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @ManyToOne
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @ManyToOne
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    @Column(name = "home_score")
    private Integer homeScore;

    @Column(name = "away_score")
    private Integer awayScore;

    @Column(name = "match_utc")
    private Instant matchUtc;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void touchUpdatedAt() {
        this.updatedAt = Instant.now();
    }
}

