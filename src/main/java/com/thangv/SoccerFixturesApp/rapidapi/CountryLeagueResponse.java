package com.thangv.SoccerFixturesApp.rapidapi;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CountryLeagueResponse {
    private String ccode;
    private String name;
    private String localizedName;
    private List<LeagueItem> leagues;
}
