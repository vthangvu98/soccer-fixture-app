package com.thangv.SoccerFixturesApp.domain;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class EmailSubscribeRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "TEAM|LEAGUE")
    private String targetType;

    @NotNull
    @Positive
    private Integer targetId;

    @Min(0)
    @Max(1440) // max 24 hours
    private Integer minutesBeforeKickoff = 60;

    @NotBlank
    private String timezone = "UTC";
}
