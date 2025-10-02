package com.thangv.SoccerFixturesApp.controller;

import com.thangv.SoccerFixturesApp.domain.LeagueDto;
import com.thangv.SoccerFixturesApp.entity.League;
import com.thangv.SoccerFixturesApp.repository.LeagueRepository;
import com.thangv.SoccerFixturesApp.service.LeagueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leagues")
@RequiredArgsConstructor
public class LeagueController {

    private final LeagueRepository leagueRepository;
    private final LeagueService leagueService;

    @GetMapping
    public List<League> getAll() {
        return leagueRepository.findAll();
    }

    @PostMapping("/import/all")
    public String importLeague() {
        Integer result = leagueService.importAllLeagueByCountry();
        return "Imported " + result + " leagues";
    }

    @PostMapping("import/single")
    public void importSingleLeague(@RequestParam LeagueDto league) {
        leagueService.importSingleLeague(league);
    }
}
