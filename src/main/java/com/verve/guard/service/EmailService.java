package com.verve.guard.service;

import com.verve.guard.notificationDTO.DepositNotification;
import com.verve.guard.notificationDTO.TransferNotification;
import com.verve.guard.notificationDTO.VerveNotification;
import com.verve.guard.notificationDTO.WithdrawalNotification;


public interface EmailService {
    void sendFraudEmail(VerveNotification verveNotification);

    //
    void sendWithdrawalEmailNotification(WithdrawalNotification withdrawalNotification);

    void sendTransferEmailNotification(TransferNotification transferNotification);

    void sendTransferEmailNotificationForDebit(TransferNotification transferNotification);

    void sendDepositEmailNotification(DepositNotification depositNotification);

}
