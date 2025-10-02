package com.thangv.SoccerFixturesApp.rapidapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchDetail {
    private long id;
    private int leagueId;
    private String time;
    private TeamDetail home;
    private TeamDetail away;
    private MatchStatus status;
}
