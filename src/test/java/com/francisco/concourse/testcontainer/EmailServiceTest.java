package com.francisco.concourse.testcontainer;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {EmailService.class, EmailServiceTest.EmailServiceTestConfiguration.class})
@Slf4j
public class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @Autowired
    private RestTemplate restTemplate;

    @ClassRule
    public static GenericContainer mailCatcherContainer = new GenericContainer("schickling/mailcatcher")
            .withExposedPorts(1025, 1080);

    @Test
    public void shouldSendEmails() {
        //Given
        log.info(
                "mail catcher ports, smpt: {}, web: {}",
                mailCatcherContainer.getMappedPort(1025),
                mailCatcherContainer.getMappedPort(1080)
        );

        final String mailCatcherMessagesUrl =
                format("http://localhost:%s/messages", mailCatcherContainer.getMappedPort(1080));

        var emailContext = EmailService.EmailContext.builder()
                .from("sender@example.es")
                .recipients(List.of("a@example.es", "b@example.es"))
                .subject("example mail")
                .content("My nice content in the email!!")
                .build();

        //When
        emailService.sendTextEmail(emailContext);

        //Then
        MailCatherMessage[] mailCatcherMessages =
                restTemplate.getForObject(mailCatcherMessagesUrl, MailCatherMessage[].class);

        assertThat(mailCatcherMessages).isNotNull();
        assertThat(mailCatcherMessages).hasSize(2);
        assertThat(mailCatcherMessages[0]).isNotNull().extracting("sender", "recipients", "subject")
                .containsExactly("<sender@example.es>", List.of("<a@example.es>"), "example mail");
        assertThat(mailCatcherMessages[1]).isNotNull().extracting("sender", "recipients", "subject")
                .containsExactly("<sender@example.es>", List.of("<b@example.es>"), "example mail");

    }

    @TestConfiguration
    static class EmailServiceTestConfiguration {

        @Bean
        JavaMailSender javaMailSender() {
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost("localhost");
            mailSender.setPort(mailCatcherContainer.getMappedPort(1025));
            return mailSender;
        }

        @Bean
        RestTemplate restTemplate() {
            return new RestTemplate();
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class MailCatherMessage {
        private String sender;
        private List<String> recipients;
        private String subject;
    }
}