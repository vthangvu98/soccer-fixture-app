package com.thangv.SoccerFixturesApp.rapidapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamDetail {
    private int id;
    private int score;
    private String name;
    private String longName;
}
