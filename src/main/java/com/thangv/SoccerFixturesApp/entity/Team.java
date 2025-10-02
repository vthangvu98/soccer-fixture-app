package com.thangv.SoccerFixturesApp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "teams")
@Getter
@Setter
public class Team {
    @Id
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(name = "short_name")
    private String shortName;

    @Column(name = "logo_url")
    private String logoUrl;

    @ManyToOne
    @JoinColumn(name = "league_id")
    private League league;

    @Column(name = "updated_at")
    private Instant updatedAt;
}

