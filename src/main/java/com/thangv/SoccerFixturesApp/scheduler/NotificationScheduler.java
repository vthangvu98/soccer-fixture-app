package com.thangv.SoccerFixturesApp.scheduler;

import com.thangv.SoccerFixturesApp.component.EmailTemplateRenderer;
import com.thangv.SoccerFixturesApp.domain.FixtureEmailModel;
import com.thangv.SoccerFixturesApp.domain.NotificationEmail;
import com.thangv.SoccerFixturesApp.entity.Fixture;
import com.thangv.SoccerFixturesApp.entity.NotificationReceipt;
import com.thangv.SoccerFixturesApp.entity.NotificationSubscription;
import com.thangv.SoccerFixturesApp.repository.FixtureRepository;
import com.thangv.SoccerFixturesApp.repository.NotificationReceiptRepository;
import com.thangv.SoccerFixturesApp.repository.NotificationSubscriptionRepository;
import com.thangv.SoccerFixturesApp.service.SesEmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Automatically sends fixture notifications to subscribed users.
 * Runs every 5 minutes to check for upcoming matches.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationScheduler {

    private final FixtureRepository fixtureRepo;
    private final NotificationSubscriptionRepository subscriptionRepo;
    private final NotificationReceiptRepository receiptRepo;
    private final EmailTemplateRenderer renderer;
    private final SesEmailSender emailSender;

    /**
     * Runs every 5 minutes to send pre-match notifications.
     * Looks ahead up to 2 hours for fixtures that users want to be notified about.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes = 300,000 milliseconds
    @Transactional
    public void sendPreMatchNotifications() {
        log.info("Starting scheduled notification check...");

        Instant now = Instant.now();
        Instant twoHoursFromNow = now.plus(2, ChronoUnit.HOURS);

        // Find all fixtures starting in the next 2 hours
        List<Fixture> upcomingFixtures = fixtureRepo.findStartingBetween(now, twoHoursFromNow);

        if (upcomingFixtures.isEmpty()) {
            log.info("No upcoming fixtures in the next 2 hours");
            return;
        }

        log.info("Found {} upcoming fixtures", upcomingFixtures.size());

        int emailsSent = 0;
        int emailsSkipped = 0;

        for (Fixture fixture : upcomingFixtures) {
            try {
                int sent = processFixtureNotifications(fixture, now);
                emailsSent += sent;
            } catch (Exception e) {
                log.error("Error processing notifications for fixture {}: {}",
                        fixture.getId(), e.getMessage(), e);
                emailsSkipped++;
            }
        }

        log.info("Notification check complete. Sent: {}, Skipped: {}", emailsSent, emailsSkipped);
    }

    /**
     * Process notifications for a single fixture.
     *
     * @param fixture The fixture to notify about
     * @param now     Current time
     * @return Number of emails sent
     */
    private int processFixtureNotifications(Fixture fixture, Instant now) {
        if (fixture.getMatchUtc() == null) {
            log.warn("Fixture {} has no match time, skipping", fixture.getId());
            return 0;
        }

        long minutesUntilKickoff = ChronoUnit.MINUTES.between(now, fixture.getMatchUtc());

        log.info("Processing fixture {} ({} vs {}) - starts in {} minutes",
                fixture.getId(),
                fixture.getHomeTeam().getName(),
                fixture.getAwayTeam().getName(),
                minutesUntilKickoff);

        // Find all subscriptions for this fixture
        List<NotificationSubscription> subscriptions = findRelevantSubscriptions(fixture);

        log.info("Found {} subscriptions for fixture {}", subscriptions.size(), fixture.getId());

        if (subscriptions.isEmpty()) {
            log.warn("No subscriptions found for fixture {} (League: {}, Home Team: {}, Away Team: {})",
                    fixture.getId(),
                    fixture.getLeague().getId(),
                    fixture.getHomeTeam().getId(),
                    fixture.getAwayTeam().getId());
            return 0;
        }

        int emailsSent = 0;

        for (NotificationSubscription sub : subscriptions) {
            log.info("Checking subscription: User={}, TargetType={}, TargetId={}, MinutesBeforeKickoff={}",
                    sub.getUser().getId(),
                    sub.getTargetType(),
                    sub.getTargetId(),
                    sub.getMinutesBeforeKickoff());

            // Check if user wants notification at this time
            if (minutesUntilKickoff <= sub.getMinutesBeforeKickoff()
                    && minutesUntilKickoff >= (sub.getMinutesBeforeKickoff() - 5)) {

                log.info("Notification time window matched! Minutes until kickoff: {}, User wants notification at: {} minutes",
                        minutesUntilKickoff, sub.getMinutesBeforeKickoff());

                // Check if we already sent this notification
                if (alreadySent(sub.getUser().getId(), fixture.getId())) {
                    log.info("Already sent notification for user {} and fixture {}",
                            sub.getUser().getId(), fixture.getId());
                    continue;
                }

                // Send the email
                try {
                    log.info("About to send email to {} for fixture {}",
                            sub.getUser().getEmail(), fixture.getId());
                    sendNotificationEmail(sub, fixture);
                    recordNotificationSent(sub.getUser().getId(), fixture.getId());
                    emailsSent++;
                    log.info("Successfully sent email to {} for fixture {}",
                            sub.getUser().getEmail(), fixture.getId());
                } catch (Exception e) {
                    log.error("Failed to send email to {} for fixture {}: {}",
                            sub.getUser().getEmail(), fixture.getId(), e.getMessage(), e);
                }
            } else {
                log.info("Not in notification window. Minutes until kickoff: {}, User wants notification at: {} minutes (window: {}-{})",
                        minutesUntilKickoff,
                        sub.getMinutesBeforeKickoff(),
                        sub.getMinutesBeforeKickoff() - 5,
                        sub.getMinutesBeforeKickoff());
            }
        }

        return emailsSent;
    }

    /**
     * Find all subscriptions relevant to this fixture (by team or league).
     */
    private List<NotificationSubscription> findRelevantSubscriptions(Fixture fixture) {
        // Get league subscriptions
        List<NotificationSubscription> leagueSubs =
                subscriptionRepo.findEmailLeagueSubs(fixture.getLeague().getId());

        // Get team subscriptions (home and away)
        Set<Integer> teamIds = Set.of(
                fixture.getHomeTeam().getId(),
                fixture.getAwayTeam().getId()
        );
        List<NotificationSubscription> teamSubs =
                subscriptionRepo.findEmailTeamSubs(teamIds);

        // Combine and deduplicate by user
        Map<Long, NotificationSubscription> uniqueSubs = new HashMap<>();
        leagueSubs.forEach(sub -> uniqueSubs.put(sub.getUser().getId(), sub));
        teamSubs.forEach(sub -> uniqueSubs.put(sub.getUser().getId(), sub));

        return new ArrayList<>(uniqueSubs.values());
    }

    /**
     * Check if we already sent this notification.
     */
    private boolean alreadySent(Long userId, Long fixtureId) {
        return receiptRepo.existsByUserIdAndFixtureIdAndKindAndChannel(
                userId, fixtureId, "PREMATCH", "EMAIL"
        );
    }

    /**
     * Record that we sent this notification.
     */
    private void recordNotificationSent(Long userId, Long fixtureId) {
        NotificationReceipt receipt = new NotificationReceipt();
        receipt.setUserId(userId);
        receipt.setFixtureId(fixtureId);
        receipt.setKind("PREMATCH");
        receipt.setChannel("EMAIL");
        receipt.setSentAt(Instant.now());
        receiptRepo.save(receipt);
    }

    /**
     * Send the actual email notification.
     * Uses @Transactional to ensure lazy-loaded relationships are accessible.
     */
    @Transactional
    private void sendNotificationEmail(NotificationSubscription sub, Fixture fixture) {
        var user = sub.getUser();
        var home = fixture.getHomeTeam();
        var away = fixture.getAwayTeam();
        var league = fixture.getLeague();

        // Convert to user's timezone
        ZoneId userZone = ZoneId.of(user.getTimezone());
        ZonedDateTime kickoffLocal = ZonedDateTime.ofInstant(fixture.getMatchUtc(), userZone);

        // Calculate match status
        ZonedDateTime now = ZonedDateTime.now(userZone);
        String matchStatus;
        if (kickoffLocal.isAfter(now)) {
            matchStatus = "UPCOMING";
        } else if (kickoffLocal.plusHours(2).isAfter(now)) {
            matchStatus = "LIVE";
        } else {
            matchStatus = "ENDED";
        }

        // Build email model with builder pattern
        FixtureEmailModel model = FixtureEmailModel.builder()
                .leagueName(league.getName())
                .homeTeamName(home.getName())
                .awayTeamName(away.getName())
                .homeTeamLogo(home.getLogoUrl())
                .awayTeamLogo(away.getLogoUrl())
                .kickoffLocal(kickoffLocal)
                .kickoffTz(userZone.getId())
                .homeScore(fixture.getHomeScore())
                .awayScore(fixture.getAwayScore())
                .matchStatus(matchStatus)
                .build();

        // Render HTML
        String html = renderer.renderFixture(model);

        // Prepare subject
        String subject = String.format("⚽ Upcoming Match: %s vs %s",
                home.getName(), away.getName());

        // Send via SES
        NotificationEmail email = new NotificationEmail(user.getEmail(), subject, html);
        emailSender.send(email);

        log.info("Sent notification to {} for fixture {} ({} vs {})",
                user.getEmail(), fixture.getId(), home.getName(), away.getName());
    }
}