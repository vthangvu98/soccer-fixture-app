package com.thangv.SoccerFixturesApp.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FixtureEmailModel {

    private String leagueName;
    private String homeTeamName;
    private String awayTeamName;
    private String homeTeamLogo;
    private String awayTeamLogo;
    private ZonedDateTime kickoffLocal;
    private String kickoffTz;
    private Integer homeScore;
    private Integer awayScore;
    private String matchStatus;  // "UPCOMING", "LIVE", "ENDED"
}