package com.verve.guard.kafka;

import com.verve.guard.notificationDTO.DepositNotification;
import com.verve.guard.notificationDTO.TransferNotification;
import com.verve.guard.notificationDTO.VerveNotification;
import com.verve.guard.notificationDTO.WithdrawalNotification;
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

    @KafkaListener(topics = "verve-guard-topic", groupId = "verve-guard-group")
    public void consumeDutyEmailNotification(VerveNotification verveNotification) {
        log.info(format("Consuming the message from transfer-topic Topic:: %s", verveNotification));

        emailService.sendFraudEmail(
                verveNotification
        );
    }

    @KafkaListener(topics = "deposit-topic", groupId = "deposit-group")
    public void consumeDepositEmailNotification(DepositNotification depositNotification) {
        log.info(format("Consuming the message from deposit-topic Topic:: %s", depositNotification));
        //
        emailService.sendDepositEmailNotification(depositNotification);
    }

    @KafkaListener(topics = "withdrawal-topic", groupId = "withdrawal-group")
    public void consumeWithdrawalEmailNotification(WithdrawalNotification withdrawalNotification) {
        log.info(format("Consuming the message from withdrawal-topic Topic:: %s", withdrawalNotification));
        //
        emailService.sendWithdrawalEmailNotification(withdrawalNotification);
    }

    @KafkaListener(topics = "transfer-topic", groupId = "transfer-credit-group")
    public void sendTransferEmailNotification(TransferNotification transferNotification) {
        log.info(format("Consuming the message from transfer-topic Topic:: %s", transferNotification));
        //
        emailService.sendTransferEmailNotification(transferNotification);
    }

    @KafkaListener(topics = "transfer-topic", groupId = "transfer-debit-group")
    public void sendTransferEmailNotificationForDebit(TransferNotification transferNotification) {
        log.info(format("Consuming the message from transfer-topic Topic:: %s", transferNotification));
        //
        emailService.sendTransferEmailNotificationForDebit(transferNotification);
    }

}
