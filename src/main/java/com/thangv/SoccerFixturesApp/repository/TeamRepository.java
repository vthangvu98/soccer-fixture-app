package com.thangv.SoccerFixturesApp.repository;

import com.thangv.SoccerFixturesApp.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface TeamRepository extends JpaRepository<Team, Integer> {

    @Query(value = "select id from teams where league_id in (:leagueIds)", nativeQuery = true)
    Set<Integer> findTeamIdsByLeagueIds(@Param("leagueIds") Set<Integer> leagueIds);
}
