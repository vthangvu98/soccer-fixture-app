package com.thangv.SoccerFixturesApp.service;

import com.thangv.SoccerFixturesApp.component.EmailTemplateRenderer;
import com.thangv.SoccerFixturesApp.domain.FixtureEmailModel;
import com.thangv.SoccerFixturesApp.domain.NotificationEmail;
import com.thangv.SoccerFixturesApp.entity.Fixture;
import com.thangv.SoccerFixturesApp.entity.League;
import com.thangv.SoccerFixturesApp.repository.LeagueRepository;
import com.thangv.SoccerFixturesApp.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailTemplateRenderer renderer;
    private final SesEmailSender sesEmailSender;
    private final TeamRepository teamRepo;
    private final LeagueRepository leagueRepo;

    public void sendFixtureEmail(String to, Fixture fixture, String timezone, String subject) {
        var home = teamRepo.getReferenceById(fixture.getHomeTeam().getId());
        var away = teamRepo.getReferenceById(fixture.getAwayTeam().getId());
        var leagueName = leagueRepo.findById(fixture.getLeague().getId())
                .map(League::getName).orElse("League");

        var zone = ZoneId.of(timezone == null ? "UTC" : timezone);
        var kickoffLocal = fixture.getMatchUtc() != null
                ? ZonedDateTime.ofInstant(fixture.getMatchUtc(), zone)
                : null;

        var model = new FixtureEmailModel(
                leagueName, home.getName(), away.getName(),
                kickoffLocal, zone.getId(),
                fixture.getHomeScore(), fixture.getAwayScore()
        );

        String html = renderer.renderFixture(model);
        sesEmailSender.send(new NotificationEmail(to, subject, html));
    }
}

