package com.thangv.SoccerFixturesApp.domain;

import lombok.Builder;

@Builder
public class FixtureEmailResponse {
    boolean success;
    String message;
    String to;
    long fixtureId;
    String subject;
}

