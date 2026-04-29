package com.verve.guard.notificationDTO;

import java.math.BigDecimal;

public record TransferNotification(
        String senderFirstName,
        String senderLastName,
        String senderEmail,
        Long senderAccountNumber,
        String receiverFirstName,
        String receiverLastName,
        String receiverEmail,
        Long receiverAccountNumber,
        BigDecimal transferAmount
) {
}
