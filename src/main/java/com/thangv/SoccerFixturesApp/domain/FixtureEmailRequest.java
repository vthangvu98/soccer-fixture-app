package com.thangv.SoccerFixturesApp.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class FixtureEmailRequest {
    @Positive
    private long fixtureId;

    @NotBlank
    @Email
    private String to;

    private String timezone = "UTC";  // default
    private String subject = "Fixture Reminder"; // default
}

