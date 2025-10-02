package com.thangv.SoccerFixturesApp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "notification_subscriptions",
        uniqueConstraints = @UniqueConstraint(name = "ux_user_target_channel",
                columnNames = {"user_id", "channel", "target_type", "target_id"}))
@Getter
@Setter
public class NotificationSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String channel;      // EMAIL | SMS

    @Column(name = "target_type", nullable = false)
    private String targetType; // TEAM | LEAGUE

    @Column(name = "target_id", nullable = false)
    private Integer targetId;

    @Column(name = "minutes_before_kickoff", nullable = false)
    private Integer minutesBeforeKickoff = 60;

    @Column(nullable = false)
    private Boolean active = true;
    
    @Column(name = "created_at")
    private java.time.Instant createdAt;

    @PrePersist
    void pre() {
        if (createdAt == null) createdAt = java.time.Instant.now();
    }
}

