package com.thangv.SoccerFixturesApp.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

@Getter
@Setter
public class EmailSender {

    @Value("${aws.ses.from}")
    private String from;
    private String to;
    private String subject;
    private String body;
}
