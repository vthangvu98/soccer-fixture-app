package com.thangv.SoccerFixturesApp.rapidapi;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeagueRapidApiResponse {
    private String status;
    private LeagueResponseList response;
}
