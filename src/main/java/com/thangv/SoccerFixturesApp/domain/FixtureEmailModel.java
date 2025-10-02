package com.thangv.SoccerFixturesApp.domain;

import java.time.ZonedDateTime;

public record FixtureEmailModel(String leagueName, String homeTeamName, String awayTeamName, ZonedDateTime kickoffLocal,
                                String kickoffTz, Integer homeScore, Integer awayScore) {
}

