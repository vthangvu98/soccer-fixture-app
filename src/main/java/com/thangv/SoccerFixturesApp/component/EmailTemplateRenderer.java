package com.thangv.SoccerFixturesApp.component;

import com.thangv.SoccerFixturesApp.domain.FixtureEmailModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Component
@RequiredArgsConstructor
public class EmailTemplateRenderer {

    private final SpringTemplateEngine engine;

    public String renderFixture(FixtureEmailModel model) {
        var ctx = new Context();
        ctx.setVariable("leagueName", model.leagueName());
        ctx.setVariable("homeTeamName", model.homeTeamName());
        ctx.setVariable("awayTeamName", model.awayTeamName());
        ctx.setVariable("kickoffLocal", model.kickoffLocal());
        ctx.setVariable("kickoffTz", model.kickoffTz());
        ctx.setVariable("homeScore", model.homeScore());
        ctx.setVariable("awayScore", model.awayScore());
        return engine.process("fixture-email", ctx); // matches templates/fixture-email.html
    }
}

