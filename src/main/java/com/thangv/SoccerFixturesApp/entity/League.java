package com.thangv.SoccerFixturesApp.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "leagues")
@Getter
@Setter
public class League {

    @Id
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String country;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "logo_url")
    @Nullable
    private String logoUrl;
}


