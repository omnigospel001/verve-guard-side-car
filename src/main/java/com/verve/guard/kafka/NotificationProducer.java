package com.verve.guard.kafka;

import com.verve.guard.notificationDTO.DepositNotification;
import com.verve.guard.notificationDTO.TransferNotification;
import com.verve.guard.notificationDTO.VerveNotification;
import com.verve.guard.notificationDTO.WithdrawalNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import static org.springframework.kafka.support.KafkaHeaders.TOPIC;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendNotification(VerveNotification verveNotification) {
        log.info("Sending notification with body = : {} :", verveNotification);
        Message<VerveNotification> message = MessageBuilder
                .withPayload(verveNotification)
                .setHeader(TOPIC, "verve-guard-topic")
                .build();

        kafkaTemplate.send(message);
    }

    public void sendTransferNotification(TransferNotification transferNotification) {
        log.info("Sending notification with body = : {} :", transferNotification);
        Message<TransferNotification> message = MessageBuilder
                .withPayload(transferNotification)
                .setHeader(TOPIC, "transfer-topic")
                .build();

        kafkaTemplate.send(message);
    }

    public void sendWithdrawalNotification(WithdrawalNotification withdrawalNotification) {
        log.info("Sending notification with body = : {} :", withdrawalNotification);
        Message<WithdrawalNotification> message = MessageBuilder
                .withPayload(withdrawalNotification)
                .setHeader(TOPIC, "withdrawal-topic")
                .build();

        kafkaTemplate.send(message);
    }

    public void sendDepositNotification(DepositNotification depositNotification) {
        log.info("Sending notification with body = : {} :", depositNotification);
        Message<DepositNotification> message = MessageBuilder
                .withPayload(depositNotification)
                .setHeader(TOPIC, "deposit-topic")
                .build();

        kafkaTemplate.send(message);
    }

}
