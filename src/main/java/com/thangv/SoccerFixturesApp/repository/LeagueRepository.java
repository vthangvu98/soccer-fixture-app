package com.thangv.SoccerFixturesApp.repository;

import com.thangv.SoccerFixturesApp.entity.League;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface LeagueRepository extends JpaRepository<League, Integer> {

    @Query(value = "SELECT id FROM leagues", nativeQuery = true)
    Set<Integer> findAllLeagueCode();
}

