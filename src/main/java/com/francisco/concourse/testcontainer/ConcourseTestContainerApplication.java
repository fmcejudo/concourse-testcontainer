package com.francisco.concourse.testcontainer;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Wither;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SpringBootApplication
public class ConcourseTestContainerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConcourseTestContainerApplication.class, args);
    }

}

@Service
class EmailService {

    private final JavaMailSender javaMailSender;

    EmailService(final JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    void sendTextEmail(final EmailContext emailContext) {
        emailContext.simpleMailMessages()
                .forEach(javaMailSender::send);
    }

    @Data
    @Builder
    static class EmailContext {

        @Wither
        private final String from;
        private final List<String> recipients;
        private final String subject;
        private final String content;

        List<SimpleMailMessage> simpleMailMessages() {

            if (StringUtils.isEmpty(from)) {
                return Collections.emptyList();
            }

            return recipients.stream()
                    .map(this::simpleMailMessage)
                    .flatMap(Optional::stream)
                    .collect(Collectors.toList());
        }

        private Optional<SimpleMailMessage> simpleMailMessage(final String to) {
            if (StringUtils.isEmpty(to)) {
                return Optional.empty();
            }
            var simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setFrom(this.from);
            simpleMailMessage.setTo(to);
            simpleMailMessage.setSubject(Optional.ofNullable(subject).orElse("Empty subject"));
            simpleMailMessage.setText(content);

            return Optional.of(simpleMailMessage);
        }
    }
}


@Component
class ConcourseTestContainerCLR implements CommandLineRunner {

    private final EmailService emailService;


    ConcourseTestContainerCLR(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void run(String... args) {

        var emailContext = EmailService.EmailContext.builder()
                .from("sender@example.es")
                .recipients(List.of("a@example.es", "b@example.es"))
                .subject("example mail")
                .content("My nice content in the email!!")
                .build();
        emailService.sendTextEmail(emailContext);
    }
}
