package com.thangv.SoccerFixturesApp.controller;

import com.thangv.SoccerFixturesApp.domain.FixtureEmailRequest;
import com.thangv.SoccerFixturesApp.domain.FixtureEmailResponse;
import com.thangv.SoccerFixturesApp.repository.FixtureRepository;
import com.thangv.SoccerFixturesApp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final FixtureRepository fixtureRepository;

    @PostMapping("/email/fixture")
    public FixtureEmailResponse sendFixtureEmail(@RequestBody FixtureEmailRequest request) {
        log.info("Received email request for fixture {} to {}", request.getFixtureId(), request.getTo());

        var fixture = fixtureRepository.findById(request.getFixtureId())
                .orElseThrow(() -> new IllegalArgumentException("Fixture not found: " + request.getFixtureId()));

        try {
            String subject = (request.getSubject() != null && !request.getSubject().isBlank())
                    ? request.getSubject()
                    : String.format("⚽ Upcoming Match: %s vs %s",
                    fixture.getHomeTeam().getName(),
                    fixture.getAwayTeam().getName());

            String timezone = (request.getTimezone() != null && !request.getTimezone().isBlank())
                    ? request.getTimezone()
                    : "UTC";

            notificationService.sendFixtureEmail(
                    request.getTo(),
                    fixture,
                    timezone,
                    subject
            );

            log.info("Successfully sent email to {} for fixture {}", request.getTo(), request.getFixtureId());

            return FixtureEmailResponse.builder()
                    .success(true)
                    .message("Email sent successfully")
                    .to(request.getTo())
                    .fixtureId(request.getFixtureId())
                    .subject(subject)
                    .build();

        } catch (Exception e) {
            log.error("Failed to send email to {} for fixture {}: {}",
                    request.getTo(), request.getFixtureId(), e.getMessage(), e);

            return FixtureEmailResponse.builder()
                    .success(false)
                    .message("Failed to send email: " + e.getMessage())
                    .to(request.getTo())
                    .fixtureId(request.getFixtureId())
                    .subject(request.getSubject())
                    .build();
        }
    }
}