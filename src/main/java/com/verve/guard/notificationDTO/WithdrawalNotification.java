package com.verve.guard.notificationDTO;

import java.math.BigDecimal;

public record WithdrawalNotification(
        String firstName,
        String lastName,
        String userEmail,
        Long accountNumber,
        BigDecimal withdrawalAmount
) {
}
