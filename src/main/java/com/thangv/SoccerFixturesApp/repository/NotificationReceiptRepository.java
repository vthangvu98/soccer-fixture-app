package com.thangv.SoccerFixturesApp.repository;

import com.thangv.SoccerFixturesApp.entity.NotificationReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationReceiptRepository extends JpaRepository<NotificationReceipt, Long> {

    boolean existsByUserIdAndFixtureIdAndKindAndChannel(
            Long userId,
            Long fixtureId,
            String kind,
            String channel
    );
}