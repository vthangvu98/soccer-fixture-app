package com.thangv.SoccerFixturesApp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// NotificationReceipt.java
@Entity
@Table(name = "notification_receipts",
        uniqueConstraints = @UniqueConstraint(name = "ux_user_fix_kind_channel",
                columnNames = {"user_id", "fixture_id", "kind", "channel"}))
@Getter
@Setter
public class NotificationReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "fixture_id", nullable = false)
    private Long fixtureId;

    @Column(nullable = false)
    private String kind;     // PREMATCH | FINAL

    @Column(nullable = false)
    private String channel;  // EMAIL | SMS

    @Column(name = "sent_at")
    private java.time.Instant sentAt;

    @PrePersist
    void pre() {
        if (sentAt == null) sentAt = java.time.Instant.now();
    }
}
