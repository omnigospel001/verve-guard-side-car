package com.verve.guard.serviceImpl;

import com.verve.guard.response.DeviceResponse;
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


    @Override
    @Async
    public void sendVerificationEmail(DeviceResponse deviceResponse) {

        try {

            MimeMessage message = mailSender.createMimeMessage();
            var messageHelper = new MimeMessageHelper(message);

            messageHelper.setFrom(senderEmail, deviceResponse.getAdminFirstName() + " " + deviceResponse.getAdminLastName());
            messageHelper.setTo(deviceResponse.getFraudsterEmail());
            messageHelper.setSubject(subjectEmail);
            messageHelper.setText(emailBody.adminEmailBody(deviceResponse), true);

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
