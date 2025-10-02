package com.thangv.SoccerFixturesApp.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class FixtureImportResult {
    private final int savedCount;
    private final Set<Integer> missingLeagueIds;
    private final Set<Integer> missingTeamIds;
}

