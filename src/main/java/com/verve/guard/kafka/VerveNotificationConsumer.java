package com.verve.guard.kafka;

import com.verve.guard.response.DeviceResponse;
import com.verve.guard.service.EmailService;
import org.springframework.kafka.annotation.KafkaListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static java.lang.String.format;

@Service
@Slf4j
@RequiredArgsConstructor
public class VerveNotificationConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = "transfer-topic", groupId = "transfer-group")
    public void consumeDutyEmailNotification(DeviceResponse deviceResponse) {
        log.info(format("Consuming the message from transfer-topic Topic:: %s", deviceResponse));

        emailService.sendVerificationEmail(
             deviceResponse
        );
    }

}
