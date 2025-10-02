package com.thangv.SoccerFixturesApp.controller;

import com.thangv.SoccerFixturesApp.client.RapidApiClient;
import com.thangv.SoccerFixturesApp.domain.FixtureImportResult;
import com.thangv.SoccerFixturesApp.entity.Fixture;
import com.thangv.SoccerFixturesApp.repository.FixtureRepository;
import com.thangv.SoccerFixturesApp.service.FixtureService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fixtures")
@RequiredArgsConstructor
public class FixtureController {

    private final RapidApiClient client;
    private final FixtureService fixtureService;
    private final FixtureRepository fixtureRepository;

    @PostMapping("/importByDate")
    public FixtureImportResult importFixtureByDate(@RequestParam String date) {
        return fixtureService.importFixtureByDate(date);
    }

    @GetMapping("/all")
    public List<Fixture> getAll() {
        return fixtureRepository.findAll();
    }
}
