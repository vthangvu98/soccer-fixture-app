package com.thangv.SoccerFixturesApp.controller;

import com.thangv.SoccerFixturesApp.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping("importByLeagueId/all")
    public Integer importTeamsByLeagueId() {
        return teamService.importAllTeamsByLeague();
    }

    @PostMapping("importByLeagueId/{leagueId}")
    public Integer importTeamsByLeagueId(@PathVariable Integer leagueId) {
        return teamService.importTeamsByLeagueId(leagueId);
    }
}
