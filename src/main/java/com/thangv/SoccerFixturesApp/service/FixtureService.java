package com.thangv.SoccerFixturesApp.service;

import com.thangv.SoccerFixturesApp.client.RapidApiClient;
import com.thangv.SoccerFixturesApp.domain.FixtureImportResult;
import com.thangv.SoccerFixturesApp.entity.Fixture;
import com.thangv.SoccerFixturesApp.rapidapi.LeagueBlock;
import com.thangv.SoccerFixturesApp.rapidapi.MatchDetail;
import com.thangv.SoccerFixturesApp.repository.FixtureRepository;
import com.thangv.SoccerFixturesApp.repository.LeagueRepository;
import com.thangv.SoccerFixturesApp.repository.TeamRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class FixtureService {

    private static final int BATCH_SIZE = 200;

    private final RapidApiClient rapidApiClient;
    private final FixtureRepository fixtureRepository;
    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;

    @Transactional
    public FixtureImportResult importFixtureByDate(String date) {
        var response = rapidApiClient.fetchFixturesByDate(date);

        if (response == null
                || !"success".equalsIgnoreCase(response.getStatus())
                || response.getResponse() == null
                || response.getResponse().isEmpty()) {
            return new FixtureImportResult(0, Set.of(), Set.of());
        }

        final Set<Integer> availableLeagueIds = leagueRepository.findAllLeagueCode();
        final Set<Integer> availableTeamIds = teamRepository.findTeamIdsByLeagueIds(availableLeagueIds);

        final var missingLeagues = new HashSet<Integer>();
        final var missingTeams = new HashSet<Integer>();
        final Instant now = Instant.now();

        var fixtures = response.getResponse().stream()
                .filter(leagueBlock -> leagueBlock != null && leagueBlock.getMatches() != null && !leagueBlock.getMatches().isEmpty())
                .flatMap(leagueBlock -> leagueToFixtures(leagueBlock, availableLeagueIds, availableTeamIds, missingLeagues, missingTeams, now))
                .toList();

        saveInBatches(fixtures);

        if (!missingLeagues.isEmpty()) {
            log.warn("Missing leagues (not in DB): {}", missingLeagues);
        }
        if (!missingTeams.isEmpty()) {
            log.warn("Missing teams (not in DB): {}", missingTeams);
        }

        return new FixtureImportResult(fixtures.size(), Collections.unmodifiableSet(missingLeagues), Collections.unmodifiableSet(missingTeams));
    }

    private Stream<Fixture> leagueToFixtures(LeagueBlock leagueBlock, Set<Integer> availableLeagueIds, Set<Integer> availableTeamIds,
                                             Set<Integer> missingLeagues, Set<Integer> missingTeams, Instant now) {
        if (!availableLeagueIds.contains(leagueBlock.getId())) {
            missingLeagues.add(leagueBlock.getId());
            return Stream.empty();
        }

        return leagueBlock.getMatches().stream()
                .filter(matchDetail -> matchDetail != null
                        && matchDetail.getHome() != null && matchDetail.getAway() != null)
                .flatMap(matchDetail
                        -> matchToFixtureIfTeamsPresent(matchDetail, leagueBlock.getId(), availableTeamIds, missingTeams, now));
    }

    private Stream<Fixture> matchToFixtureIfTeamsPresent(MatchDetail m, int leagueId, Set<Integer> availableTeamIds,
                                                         Set<Integer> missingTeams, Instant now) {
        var homeId = m.getHome().getId();
        var awayId = m.getAway().getId();

        boolean missing = false;
        if (!availableTeamIds.contains(homeId)) {
            missingTeams.add(homeId);
            missing = true;
        }
        if (!availableTeamIds.contains(awayId)) {
            missingTeams.add(awayId);
            missing = true;
        }
        if (missing) {
            return Stream.empty();
        }

        return Stream.of(mapToFixture(m, leagueId, now));
    }

    private void saveInBatches(List<Fixture> fixtures) {
        for (int i = 0; i < fixtures.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, fixtures.size());
            fixtureRepository.saveAll(fixtures.subList(i, end));
        }
    }

    private Fixture mapToFixture(MatchDetail m, int leagueId, Instant now) {
        Instant kickoff = null;
        try {
            if (m.getStatus() != null && m.getStatus().getUtcTime() != null) {
                kickoff = Instant.parse(m.getStatus().getUtcTime());
            }
        } catch (Exception e) {
            log.warn("Could not parse kickoff time for match {}: {}", m.getId(),
                    (m.getStatus() != null ? m.getStatus().getUtcTime() : "null"));
        }

        return Fixture.builder()
                .id(m.getId())
                .league(leagueRepository.getReferenceById(leagueId))
                .homeTeam(teamRepository.getReferenceById(m.getHome().getId()))
                .homeScore(m.getHome().getScore())
                .awayTeam(teamRepository.getReferenceById(m.getAway().getId()))
                .awayScore(m.getAway().getScore())
                .matchUtc(kickoff)
                .updatedAt(now)
                .build();
    }
}

