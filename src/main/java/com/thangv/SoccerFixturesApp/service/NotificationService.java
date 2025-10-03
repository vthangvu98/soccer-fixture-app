package com.thangv.SoccerFixturesApp.service;

import com.thangv.SoccerFixturesApp.component.EmailTemplateRenderer;
import com.thangv.SoccerFixturesApp.domain.FixtureEmailModel;
import com.thangv.SoccerFixturesApp.domain.NotificationEmail;
import com.thangv.SoccerFixturesApp.entity.Fixture;
import com.thangv.SoccerFixturesApp.entity.League;
import com.thangv.SoccerFixturesApp.repository.LeagueRepository;
import com.thangv.SoccerFixturesApp.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final EmailTemplateRenderer renderer;
    private final SesEmailSender sesEmailSender;
    private final TeamRepository teamRepo;
    private final LeagueRepository leagueRepo;

    public void sendFixtureEmail(String to, Fixture fixture, String timezone, String subject) {
        log.info("Preparing fixture email for fixture {} to {}", fixture.getId(), to);

        var home = teamRepo.getReferenceById(fixture.getHomeTeam().getId());
        var away = teamRepo.getReferenceById(fixture.getAwayTeam().getId());
        var leagueName = leagueRepo.findById(fixture.getLeague().getId())
                .map(League::getName)
                .orElse("League");

        ZoneId zoneId = parseAndValidateTimezone(timezone);
        log.debug("Using timezone: {} for user {}", zoneId.getId(), to);

        ZonedDateTime kickoffLocal = null;
        if (fixture.getMatchUtc() != null) {
            kickoffLocal = ZonedDateTime.ofInstant(fixture.getMatchUtc(), zoneId);
            log.debug("Match time: {} UTC -> {} {}",
                    fixture.getMatchUtc(),
                    kickoffLocal,
                    zoneId.getId());
        } else {
            log.warn("Fixture {} has no match time set", fixture.getId());
        }

        // Build email model
        var model = new FixtureEmailModel(
                leagueName,
                home.getName(),
                away.getName(),
                kickoffLocal,
                zoneId.getId(),
                fixture.getHomeScore(),
                fixture.getAwayScore()
        );

        // Render HTML template
        String html = renderer.renderFixture(model);

        // Prepare subject with default if not provided
        String emailSubject = (subject != null && !subject.isBlank())
                ? subject
                : String.format("Upcoming Match: %s vs %s", home.getName(), away.getName());

        // Send email
        try {
            sesEmailSender.send(new NotificationEmail(to, emailSubject, html));
            log.info("Successfully sent fixture email for match {} to {}", fixture.getId(), to);
        } catch (Exception e) {
            log.error("Failed to send fixture email for match {} to {}: {}",
                    fixture.getId(), to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private ZoneId parseAndValidateTimezone(String timezone) {
        // Handle null or empty
        if (timezone == null || timezone.isBlank()) {
            log.debug("No timezone provided, defaulting to UTC");
            return ZoneId.of("UTC");
        }

        String trimmed = timezone.trim();

        try {
            ZoneId zoneId = ZoneId.of(trimmed);
            log.debug("Successfully parsed timezone: {}", zoneId.getId());
            return zoneId;
        } catch (DateTimeException e) {
            log.warn("Invalid timezone '{}' provided. Falling back to UTC. Error: {}",
                    trimmed, e.getMessage());
            return ZoneId.of("UTC");
        }
    }

    public boolean isValidTimezone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return false;
        }
        try {
            ZoneId.of(timezone.trim());
            return true;
        } catch (DateTimeException e) {
            return false;
        }
    }
}