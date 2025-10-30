package com.thangv.SoccerFixturesApp.repository;

import com.thangv.SoccerFixturesApp.entity.Fixture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface FixtureRepository extends JpaRepository<Fixture, Long> {

    @Query("""
                select f from Fixture f
                join fetch f.league
                join fetch f.homeTeam
                join fetch f.awayTeam
                where f.matchUtc is not null
                  and f.matchUtc >= :from
                  and f.matchUtc <  :to
                order by f.matchUtc
            """)
    List<Fixture> findStartingBetween(@Param("from") Instant from, @Param("to") Instant to);
}