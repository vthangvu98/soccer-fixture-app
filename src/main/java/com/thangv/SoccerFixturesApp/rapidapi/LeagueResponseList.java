package com.thangv.SoccerFixturesApp.rapidapi;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LeagueResponseList {
    private List<CountryLeagueResponse> leagues;
}
