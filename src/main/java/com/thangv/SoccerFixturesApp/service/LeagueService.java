package com.thangv.SoccerFixturesApp.service;

import com.thangv.SoccerFixturesApp.client.RapidApiClient;
import com.thangv.SoccerFixturesApp.domain.LeagueDto;
import com.thangv.SoccerFixturesApp.entity.League;
import com.thangv.SoccerFixturesApp.rapidapi.CountryLeagueResponse;
import com.thangv.SoccerFixturesApp.rapidapi.LeagueItem;
import com.thangv.SoccerFixturesApp.rapidapi.LeagueRapidApiResponse;
import com.thangv.SoccerFixturesApp.repository.LeagueRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LeagueService {

    public static final String FAILED = "failed";
    private final RapidApiClient rapidApiClient;
    private final LeagueRepository leagueRepository;

    @Transactional
    public Integer importAllLeagueByCountry() {
        LeagueRapidApiResponse leagueResponse = rapidApiClient.fetchLeagueCatalog();

        if (leagueResponse.getStatus().equals(FAILED) || leagueResponse.getResponse() == null
                || leagueResponse.getResponse().getLeagues().isEmpty()) {
            return 0;
        }
        int count = 0;
        for (CountryLeagueResponse country : leagueResponse.getResponse().getLeagues()) {
            if (country.getLeagues() == null) continue;
            for (LeagueItem leagueItem : country.getLeagues()) {
                League league = leagueRepository.findById(leagueItem.getId()).orElseGet(League::new);

                league.setId(leagueItem.getId());
                league.setName(leagueItem.getName());
                league.setCountry(country.getName());
                league.setCountryCode(country.getCcode());
                league.setLogoUrl(leagueItem.getLogo());

                leagueRepository.save(league);
                count++;
            }
        }
        return count;
    }

    public void importSingleLeague(LeagueDto leagueInput) {

        League league = new League();

        league.setName(leagueInput.getName());
        league.setCountry(leagueInput.getCountry());
        league.setCountryCode(leagueInput.getCountryCode());
        league.setLogoUrl(String.valueOf(Optional.ofNullable(leagueInput.getLogoUrl())));

        leagueRepository.save(league);
    }
}
