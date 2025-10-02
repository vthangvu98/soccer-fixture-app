package com.thangv.SoccerFixturesApp.domain;

import lombok.Data;

@Data
public class EmailSubscribeRequest {
    private String email;
    private String targetType;      // TEAM | LEAGUE
    private Integer targetId;
    private Integer minutesBeforeKickoff;
    private String timezone;
}
