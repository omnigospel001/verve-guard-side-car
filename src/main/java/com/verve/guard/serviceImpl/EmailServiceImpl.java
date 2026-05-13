package com.verve.guard.serviceImpl;

import com.verve.guard.notificationDTO.DepositNotification;
import com.verve.guard.notificationDTO.TransferNotification;
import com.verve.guard.notificationDTO.VerveNotification;
import com.verve.guard.notificationDTO.WithdrawalNotification;
import com.verve.guard.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl  implements EmailService {

    private final JavaMailSender mailSender;

    private final EmailBody emailBody;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${spring.mail.subject}")
    private String subjectEmail;

    @Value("${spring.company.name}")
    private String companyName;

    @Value("${spring.mail.admin}")
    private String adminEmail;

    @Override
    @Async
    public void sendFraudEmail(VerveNotification verveNotification) {

        try {

            MimeMessage message = mailSender.createMimeMessage();
            var messageHelper = new MimeMessageHelper(message);

            messageHelper.setFrom(senderEmail, verveNotification.adminFirstName() + " " + verveNotification.adminLastName());
            messageHelper.setTo(adminEmail);
            messageHelper.setSubject(subjectEmail);
            messageHelper.setText(emailBody.adminEmailBody(verveNotification), true);

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //
    @Override
    @Async
    public void sendDepositEmailNotification(DepositNotification depositNotification) {

        try {

            MimeMessage message = mailSender.createMimeMessage();
            var messageHelper = new MimeMessageHelper(message);

            messageHelper.setFrom(senderEmail, companyName);
            messageHelper.setTo(depositNotification.userEmail());
            messageHelper.setSubject(subjectEmail);
            messageHelper.setText(emailBody.depositCreditAlertEmailBody(depositNotification), true);

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    @Async
    public void sendWithdrawalEmailNotification(WithdrawalNotification withdrawalNotification) {

        try {

            MimeMessage message = mailSender.createMimeMessage();
            var messageHelper = new MimeMessageHelper(message);

            messageHelper.setFrom(senderEmail, companyName);
            messageHelper.setTo(withdrawalNotification.userEmail());
            messageHelper.setSubject(subjectEmail);
            messageHelper.setText(emailBody.withdrawalDebitAlertEmailBody(withdrawalNotification), true);

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    @Async
    public void sendTransferEmailNotification(TransferNotification transferNotification) {

        try {

            MimeMessage message = mailSender.createMimeMessage();
            var messageHelper = new MimeMessageHelper(message);

            messageHelper.setFrom(senderEmail, companyName);
            messageHelper.setTo(transferNotification.receiverEmail());
            messageHelper.setSubject(subjectEmail);
            messageHelper.setText(emailBody.creditAlertEmailBody(transferNotification), true);

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    @Async
    public void sendTransferEmailNotificationForDebit(TransferNotification transferNotification) {

        try {

            MimeMessage message = mailSender.createMimeMessage();
            var messageHelper = new MimeMessageHelper(message);

            messageHelper.setFrom(senderEmail, companyName);
            messageHelper.setTo(transferNotification.senderEmail());
            messageHelper.setSubject(subjectEmail);
            messageHelper.setText(emailBody.debitAlertEmailBody(transferNotification), true);

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
