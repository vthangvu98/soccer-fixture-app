package com.thangv.SoccerFixturesApp.rapidapi;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamRapidApiResponse {
    private String status;
    private TeamResponseList response;
}
