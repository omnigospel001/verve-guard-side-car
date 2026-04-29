package com.verve.guard.notificationDTO;

import java.math.BigDecimal;

public record DepositNotification(
        String firstName,
        String lastName,
        String userEmail,
        Long accountNumber,
        BigDecimal depositAmount
) {
}
