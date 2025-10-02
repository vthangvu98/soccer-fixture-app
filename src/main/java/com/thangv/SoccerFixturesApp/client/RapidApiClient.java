package com.thangv.SoccerFixturesApp.client;

import com.thangv.SoccerFixturesApp.rapidapi.FixtureApiResponse;
import com.thangv.SoccerFixturesApp.rapidapi.LeagueRapidApiResponse;
import com.thangv.SoccerFixturesApp.rapidapi.TeamRapidApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.Duration;

@Component
public class RapidApiClient {

    private final WebClient webClient;

    public RapidApiClient(
            WebClient.Builder builder,
            @Value("${rapidapi.key}") String rapidApiKey,
            @Value("${rapidapi.host}") String rapidApiHost
    ) {
        var http = reactor.netty.http.client.HttpClient.create()
                .compress(true)
                .responseTimeout(Duration.ofSeconds(30))
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .doOnConnected(c -> c
                        .addHandlerLast(new io.netty.handler.timeout.ReadTimeoutHandler(30))
                        .addHandlerLast(new io.netty.handler.timeout.WriteTimeoutHandler(30)));

        this.webClient = builder
                .baseUrl("https://free-api-live-football-data.p.rapidapi.com")
                .defaultHeader("x-rapidapi-key", rapidApiKey)
                .defaultHeader("x-rapidapi-host", rapidApiHost)
                .clientConnector(new org.springframework.http.client.reactive.ReactorClientHttpConnector(http))
                .exchangeStrategies(org.springframework.web.reactive.function.client.ExchangeStrategies.builder()
                        .codecs(c -> c.defaultCodecs().maxInMemorySize(8 * 1024 * 1024)) // 8MB
                        .build())
                .build();
    }

    public LeagueRapidApiResponse fetchLeagueCatalog() {
        return webClient.get()
                .uri("/football-get-all-leagues-with-countries")
                .retrieve()
                .bodyToMono(LeagueRapidApiResponse.class)
                .timeout(Duration.ofSeconds(20))
                .retryWhen(reactor.util.retry.Retry.backoff(2, Duration.ofMillis(300))
                        .filter(ex -> ex instanceof IOException))
                .block();
    }

    public TeamRapidApiResponse fetchTeamsByLeagueId(Integer leagueId) {
        return webClient.get()
                .uri(uri -> uri.path("/football-get-list-all-team")
                        .queryParam("leagueid", leagueId)
                        .build())
                .retrieve()
                .bodyToMono(TeamRapidApiResponse.class)
                .timeout(Duration.ofSeconds(20))
                .retryWhen(reactor.util.retry.Retry.backoff(2, Duration.ofMillis(300))
                        .filter(ex -> ex instanceof IOException))
                .block();
    }

    public FixtureApiResponse fetchFixturesByDate(String date) {
        return webClient.get()
                .uri(uri -> uri.path("/football-get-matches-by-date-and-league")
                        .queryParam("date", date)
                        .build())
                .retrieve()
                .bodyToMono(FixtureApiResponse.class)
                .timeout(Duration.ofSeconds(45))
                .retryWhen(reactor.util.retry.Retry.backoff(2, Duration.ofMillis(500))
                        .filter(ex -> ex instanceof IOException))
                .block();
    }
}

