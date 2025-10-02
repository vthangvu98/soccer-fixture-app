package com.thangv.SoccerFixturesApp.controller;

import lombok.Data;

@Data
public class FixtureEmailRequest {
    private long fixtureId;
    private String to;
    private String timezone;  // default
    private String subject; // default
}

