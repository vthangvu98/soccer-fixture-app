package com.thangv.SoccerFixturesApp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phone;

    @Column(nullable = false, name = "tz")
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
