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
        log.info("Starting fixture import for date: {}", date);
        long startTime = System.currentTimeMillis();

        var response = rapidApiClient.fetchFixturesByDate(date);

        if (response == null) {
            log.error("Received null response from RapidAPI for date: {}", date);
            return new FixtureImportResult(0, Set.of(), Set.of());
        }

        if (!"success".equalsIgnoreCase(response.getStatus())) {
            log.warn("API returned non-success status '{}' for date: {}", response.getStatus(), date);
            return new FixtureImportResult(0, Set.of(), Set.of());
        }

        if (response.getResponse() == null || response.getResponse().isEmpty()) {
            log.info("No fixtures found for date: {}", date);
            return new FixtureImportResult(0, Set.of(), Set.of());
        }

        log.debug("Received {} league blocks from API", response.getResponse().size());

        final Set<Integer> availableLeagueIds = leagueRepository.findAllLeagueCode();
        final Set<Integer> availableTeamIds = teamRepository.findTeamIdsByLeagueIds(availableLeagueIds);

        log.debug("Found {} leagues and {} teams in database",
                availableLeagueIds.size(), availableTeamIds.size());

        final var missingLeagues = new HashSet<Integer>();
        final var missingTeams = new HashSet<Integer>();
        final Instant now = Instant.now();

        var fixtures = response.getResponse().stream()
                .filter(leagueBlock -> leagueBlock != null && leagueBlock.getMatches() != null && !leagueBlock.getMatches().isEmpty())
                .flatMap(leagueBlock -> leagueToFixtures(leagueBlock, availableLeagueIds, availableTeamIds, missingLeagues, missingTeams, now))
                .toList();

        if (fixtures.isEmpty()) {
            log.warn("No valid fixtures to import for date: {} (all filtered out)", date);
        } else {
            log.info("Preparing to save {} fixtures", fixtures.size());
            saveInBatches(fixtures);
        }

        if (!missingLeagues.isEmpty()) {
            log.warn("Missing {} leagues (not in DB): {}", missingLeagues.size(), missingLeagues);
        }
        if (!missingTeams.isEmpty()) {
            log.warn("Missing {} teams (not in DB): {}", missingTeams.size(), missingTeams);
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Successfully imported {} fixtures for date: {} in {}ms",
                fixtures.size(), date, duration);

        return new FixtureImportResult(fixtures.size(),
                Collections.unmodifiableSet(missingLeagues),
                Collections.unmodifiableSet(missingTeams));
    }

    private Stream<Fixture> leagueToFixtures(LeagueBlock leagueBlock, Set<Integer> availableLeagueIds, Set<Integer> availableTeamIds,
                                             Set<Integer> missingLeagues, Set<Integer> missingTeams, Instant now) {
        if (!availableLeagueIds.contains(leagueBlock.getId())) {
            log.debug("Skipping league {} - not in database", leagueBlock.getId());
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
            log.debug("Match {} - home team {} not found in database", m.getId(), homeId);
            missingTeams.add(homeId);
            missing = true;
        }
        if (!availableTeamIds.contains(awayId)) {
            log.debug("Match {} - away team {} not found in database", m.getId(), awayId);
            missingTeams.add(awayId);
            missing = true;
        }
        if (missing) {
            return Stream.empty();
        }

        return Stream.of(mapToFixture(m, leagueId, now));
    }

    private void saveInBatches(List<Fixture> fixtures) {
        log.debug("Saving {} fixtures in batches of {}", fixtures.size(), BATCH_SIZE);

        for (int i = 0; i < fixtures.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, fixtures.size());
            int batchNumber = (i / BATCH_SIZE) + 1;
            int totalBatches = (fixtures.size() + BATCH_SIZE - 1) / BATCH_SIZE;

            log.debug("Saving batch {}/{} ({} fixtures)", batchNumber, totalBatches, end - i);
            fixtureRepository.saveAll(fixtures.subList(i, end));
        }

        log.info("Completed saving all {} fixtures", fixtures.size());
    }

    private Fixture mapToFixture(MatchDetail m, int leagueId, Instant now) {
        Instant kickoff = null;
        try {
            if (m.getStatus() != null && m.getStatus().getUtcTime() != null) {
                kickoff = Instant.parse(m.getStatus().getUtcTime());
            }
        } catch (Exception e) {
            log.warn("Could not parse kickoff time for match {}: {} - Error: {}",
                    m.getId(),
                    (m.getStatus() != null ? m.getStatus().getUtcTime() : "null"),
                    e.getMessage());
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