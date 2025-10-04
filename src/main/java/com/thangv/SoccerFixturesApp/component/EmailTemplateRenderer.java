package com.thangv.SoccerFixturesApp.component;

import com.thangv.SoccerFixturesApp.domain.FixtureEmailModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateRenderer {

    private final SpringTemplateEngine engine;

    public String renderFixture(FixtureEmailModel model) {
        log.debug("Rendering email template with model: {}", model);
        log.debug("Match status: {}", model.getMatchStatus());

        var ctx = new Context();
        ctx.setVariable("leagueName", model.getLeagueName());
        ctx.setVariable("homeTeamName", model.getHomeTeamName());
        ctx.setVariable("awayTeamName", model.getAwayTeamName());
        ctx.setVariable("homeTeamLogo", model.getHomeTeamLogo());
        ctx.setVariable("awayTeamLogo", model.getAwayTeamLogo());
        ctx.setVariable("kickoffLocal", model.getKickoffLocal());
        ctx.setVariable("kickoffTz", model.getKickoffTz());
        ctx.setVariable("homeScore", model.getHomeScore());
        ctx.setVariable("awayScore", model.getAwayScore());
        ctx.setVariable("matchStatus", model.getMatchStatus());
        return engine.process("fixture-email", ctx);
    }
}