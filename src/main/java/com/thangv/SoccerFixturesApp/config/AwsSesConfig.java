package com.thangv.SoccerFixturesApp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

@Configuration
public class AwsSesConfig {

    @Bean
    Region awsRegion(@Value("${aws.region}") String region) {
        return Region.of(region);
    }

    @Bean
    SesV2Client sesClient(Region region) {
        return SesV2Client.builder().region(region).build();
    }
}
