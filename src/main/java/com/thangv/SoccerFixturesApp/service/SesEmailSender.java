package com.thangv.SoccerFixturesApp.service;

import com.thangv.SoccerFixturesApp.domain.NotificationEmail;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.*;

@Service
@RequiredArgsConstructor
public class SesEmailSender {

    private final SesV2Client ses;

    @Value("${aws.ses.from}")
    private String from;

    public void send(NotificationEmail email) {
        var req = SendEmailRequest.builder()
                .fromEmailAddress(from)
                .destination(Destination.builder().toAddresses(email.to()).build())
                .content(EmailContent.builder()
                        .simple(Message.builder()
                                .subject(Content.builder().data(email.subject()).build())
                                .body(Body.builder()
                                        .html(Content.builder().data(email.htmlBody()).build())
                                        .build())
                                .build())
                        .build())
                .build();
        ses.sendEmail(req);
    }
}


