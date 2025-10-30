package com.thangv.SoccerFixturesApp.repository;

import com.thangv.SoccerFixturesApp.entity.NotificationSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface NotificationSubscriptionRepository extends JpaRepository<NotificationSubscription, Long> {

    @Query("""
              select s from NotificationSubscription s
              join fetch s.user
              where s.active = true and s.channel = 'EMAIL'
                and s.targetType = 'LEAGUE' and s.targetId = :leagueId
            """)
    List<NotificationSubscription> findEmailLeagueSubs(@Param("leagueId") int leagueId);

    @Query("""
              select s from NotificationSubscription s
              join fetch s.user
              where s.active = true and s.channel = 'EMAIL'
                and s.targetType = 'TEAM' and s.targetId in :teamIds
            """)
    List<NotificationSubscription> findEmailTeamSubs(@Param("teamIds") Collection<Integer> teamIds);
}