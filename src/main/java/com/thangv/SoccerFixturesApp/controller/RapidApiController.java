package com.thangv.SoccerFixturesApp.controller;

import com.thangv.SoccerFixturesApp.client.RapidApiClient;
import com.thangv.SoccerFixturesApp.rapidapi.LeagueRapidApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rapidapi")
@RequiredArgsConstructor
public class RapidApiController {

    private final RapidApiClient rapidApiClient;

    @GetMapping("/league")
    public LeagueRapidApiResponse getLeague() {
        return rapidApiClient.fetchLeagueCatalog();
    }
}
