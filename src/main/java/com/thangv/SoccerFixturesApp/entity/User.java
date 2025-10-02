package com.thangv.SoccerFixturesApp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// User.java
@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;       // for EMAIL channel

    @Column(unique = true)
    private String phone;       // for SMS later

    @Column(nullable = false)
    private String timezone = "UTC";

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at")
    private java.time.Instant createdAt;

    @PrePersist
    void pre() {
        if (createdAt == null) createdAt = java.time.Instant.now();
    }
}
