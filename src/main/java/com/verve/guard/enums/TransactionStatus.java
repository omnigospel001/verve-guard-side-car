package com.verve.guard.enums;

import lombok.Getter;

@Getter
public enum TransactionStatus {
    PAYMENT_IN_PROGRESS,
    PAYMENT_SUCCESSFUL,
    PAYMENT_FAILED,
    PAYMENT_CANCEL
}
