package com.thangv.SoccerFixturesApp.controller;

import com.thangv.SoccerFixturesApp.entity.Team;
import com.thangv.SoccerFixturesApp.repository.TeamRepository;
import com.thangv.SoccerFixturesApp.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final TeamRepository teamRepository;

    @GetMapping
    public List<Team> getTeams() {
        return teamRepository.findAll();
    }

    @PostMapping("/importByLeagueId/all")
    public Map<String, Object> importAllTeamsByLeague() {
        return teamService.importAllTeamsByLeague();
    }

    @PostMapping("/importByLeagueId/{leagueId}")
    public Integer importTeamsByLeagueId(@PathVariable Integer leagueId) {
        return teamService.importTeamsByLeagueId(leagueId);
    }
}