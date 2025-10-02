package com.thangv.SoccerFixturesApp.rapidapi;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FixtureApiResponse {

    private String status;
    private List<LeagueBlock> response;
}
