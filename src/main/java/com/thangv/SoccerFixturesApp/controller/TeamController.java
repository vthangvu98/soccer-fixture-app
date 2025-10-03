package com.thangv.SoccerFixturesApp.controller;

import com.thangv.SoccerFixturesApp.entity.Team;
import com.thangv.SoccerFixturesApp.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    public List<Team> getAllTeams() {
        return teamService.getAllTeams();
    }

    @PostMapping("importByLeagueId/all")
    public Integer importTeamsByLeagueId() {
        return teamService.importAllTeamsByLeague();
    }

    @PostMapping("importByLeagueId/{leagueId}")
    public Integer importTeamsByLeagueId(@PathVariable Integer leagueId) {
        return teamService.importTeamsByLeagueId(leagueId);
    }
}
