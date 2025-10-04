package com.thangv.SoccerFixturesApp.scheduler;

import com.thangv.SoccerFixturesApp.service.FixtureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataImportScheduler {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final FixtureService fixtureService;

    /**
     * Import today's fixtures every day at 3 AM.
     * Cron: "0 0 3 * * ?" = second minute hour day month weekday
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void importTodayFixtures() {
        String today = LocalDate.now().format(DATE_FORMAT);
        log.info("Starting daily fixture import for: {}", today);

        try {
            var result = fixtureService.importFixtureByDate(today);
            log.info("Daily import complete for {}: {} fixtures processed", today, result.getSavedCount());

            if (!result.getMissingLeagueIds().isEmpty()) {
                log.warn("Missing {} leagues: {}",
                        result.getMissingLeagueIds().size(),
                        result.getMissingLeagueIds());
            }
            if (!result.getMissingTeamIds().isEmpty()) {
                log.warn("Missing {} teams: {}",
                        result.getMissingTeamIds().size(),
                        result.getMissingTeamIds());
            }
        } catch (Exception e) {
            log.error("Failed to import fixtures for {}: {}", today, e.getMessage(), e);
        }
    }

    /**
     * Import tomorrow's fixtures every day at 3:30 AM.
     * This ensures we have advance notice of upcoming matches.
     */
    @Scheduled(cron = "0 30 3 * * ?")
    public void importTomorrowFixtures() {
        String tomorrow = LocalDate.now().plusDays(1).format(DATE_FORMAT);
        log.info("Starting tomorrow's fixture import for: {}", tomorrow);

        try {
            var result = fixtureService.importFixtureByDate(tomorrow);
            log.info("Tomorrow's import complete for {}: {} fixtures imported",
                    tomorrow, result.getSavedCount());
        } catch (Exception e) {
            log.error("Failed to import fixtures for {}: {}", tomorrow, e.getMessage(), e);
        }
    }

    /**
     * Import next 7 days of fixtures once a week.
     * Runs every Sunday at 4 AM.
     */
    @Scheduled(cron = "0 0 4 ? * SUN")
    public void importNextWeekFixtures() {
        log.info("Starting weekly fixture import for next 7 days");

        int totalImported = 0;
        for (int i = 0; i < 7; i++) {
            String date = LocalDate.now().plusDays(i).format(DATE_FORMAT);
            try {
                var result = fixtureService.importFixtureByDate(date);
                totalImported += result.getSavedCount();
                log.info("Imported {} fixtures for {}", result.getSavedCount(), date);
            } catch (Exception e) {
                log.error("Failed to import fixtures for {}: {}", date, e.getMessage());
            }
        }

        log.info("Weekly import complete. Total imported: {} fixtures", totalImported);
    }
}