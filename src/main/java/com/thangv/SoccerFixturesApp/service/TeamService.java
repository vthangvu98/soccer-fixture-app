package com.thangv.SoccerFixturesApp.service;

import com.thangv.SoccerFixturesApp.client.RapidApiClient;
import com.thangv.SoccerFixturesApp.entity.League;
import com.thangv.SoccerFixturesApp.entity.Team;
import com.thangv.SoccerFixturesApp.rapidapi.TeamItem;
import com.thangv.SoccerFixturesApp.repository.LeagueRepository;
import com.thangv.SoccerFixturesApp.repository.TeamRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {

    private final RapidApiClient rapidApiClient;
    private final TeamRepository teamRepository;
    private final LeagueRepository leagueRepository;

    @Transactional
    public Integer importTeamsByLeagueId(Integer leagueId) {
        var response = rapidApiClient.fetchTeamsByLeagueId(leagueId);

        if (response == null || !"success".equalsIgnoreCase(response.getStatus())
                || response.getResponse() == null
                || response.getResponse().getList() == null
                || response.getResponse().getList().isEmpty()) {
            return 0;
        }

        var league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found: " + leagueId));

        var items = response.getResponse().getList().stream()
                .collect(Collectors.toMap(
                        TeamItem::getId,
                        teamItem -> teamItem,
                        (a, b) -> a
                ))
                .values();

        var ids = response.getResponse().getList().stream().map(TeamItem::getId)
                .collect(Collectors.toSet());
        var existing = teamRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Team::getId, t -> t));

        var now = Instant.now();
        var toSave = new ArrayList<Team>(items.size());

        for (var teamItem : items) {
            var entity = existing.getOrDefault(teamItem.getId(), new Team());
            entity.setId(teamItem.getId());
            entity.setName(teamItem.getName());
            entity.setShortName(teamItem.getShortName());
            entity.setLogoUrl(teamItem.getLogo());
            entity.setLeague(league);
            entity.setUpdatedAt(now);
            toSave.add(entity);
        }

        teamRepository.saveAll(toSave);
        return toSave.size();
    }

    @Transactional
    public Map<String, Object> importAllTeamsByLeague() {
        var leagueIds = leagueRepository.findAll().stream()
                .map(League::getId)
                .toList();

        int totalSuccess = 0;
        int totalFailed = 0;
        List<String> failedLeagues = new ArrayList<>();

        log.info("Starting team import for {} leagues", leagueIds.size());

        for (Integer leagueId : leagueIds) {
            try {
                log.info("Importing teams for league {}", leagueId);
                int count = importTeamsByLeagueId(leagueId);
                totalSuccess += count;
                log.info("Successfully imported {} teams for league {}", count, leagueId);

                // Small delay to avoid rate limiting
                Thread.sleep(500);

            } catch (Exception e) {
                totalFailed++;
                var leagueName = leagueRepository.findById(leagueId)
                        .map(League::getName)
                        .orElse("Unknown");
                failedLeagues.add(leagueName + " (ID: " + leagueId + ")");
                log.error("Failed to import teams for league {}: {}", leagueId, e.getMessage());
            }
        }

        log.info("Team import completed. Success: {}, Failed: {}", totalSuccess, totalFailed);

        return Map.of(
                "totalTeamsImported", totalSuccess,
                "totalLeaguesProcessed", leagueIds.size(),
                "failedLeaguesCount", totalFailed,
                "failedLeagues", failedLeagues
        );
    }
}