package com.thangv.SoccerFixturesApp.controller;

import com.thangv.SoccerFixturesApp.domain.EmailSubscribeRequest;
import com.thangv.SoccerFixturesApp.entity.NotificationSubscription;
import com.thangv.SoccerFixturesApp.entity.User;
import com.thangv.SoccerFixturesApp.repository.NotificationSubscriptionRepository;
import com.thangv.SoccerFixturesApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final UserRepository userRepo;
    private final NotificationSubscriptionRepository subRepo;

    @PostMapping("/email")
    public NotificationSubscription subscribeEmail(@RequestBody EmailSubscribeRequest req) {
        var user = userRepo.findByEmail(req.getEmail()).orElseGet(() -> {
            var newUser = new User();
            newUser.setEmail(req.getEmail());
            newUser.setTimezone(req.getTimezone());
            return userRepo.save(newUser);
        });

        var sub = new NotificationSubscription();
        sub.setUser(user);
        sub.setChannel("EMAIL");
        sub.setTargetType(req.getTargetType());
        sub.setTargetId(req.getTargetId());
        sub.setMinutesBeforeKickoff(req.getMinutesBeforeKickoff());
        sub.setActive(true);
        return subRepo.save(sub); // unique constraint prevents duplicates
    }
}
