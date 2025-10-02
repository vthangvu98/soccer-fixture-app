package com.thangv.SoccerFixturesApp.rapidapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeagueBlock {
    private Integer id;
    private String name;
    private List<MatchDetail> matches;
}
