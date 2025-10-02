package com.thangv.SoccerFixturesApp.controller;

import com.thangv.SoccerFixturesApp.domain.FixtureEmailResponse;
import com.thangv.SoccerFixturesApp.repository.FixtureRepository;
import com.thangv.SoccerFixturesApp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final FixtureRepository fixtureRepository;

    @PostMapping("/email/fixture")
    public FixtureEmailResponse sendFixtureEmail(@RequestBody FixtureEmailRequest request) {
        var fixture = fixtureRepository.findById(request.getFixtureId())
                .orElseThrow(() -> new IllegalArgumentException("Fixture not found: " + request.getFixtureId()));

        try {
            notificationService.sendFixtureEmail(
                    request.getTo(),
                    fixture,
                    request.getTimezone(),
                    request.getSubject()
            );

            return FixtureEmailResponse.builder()
                    .success(true)
                    .message("Email sent successfully")
                    .to(request.getTo())
                    .fixtureId(request.getFixtureId())
                    .subject(request.getSubject())
                    .build();

        } catch (Exception e) {
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
