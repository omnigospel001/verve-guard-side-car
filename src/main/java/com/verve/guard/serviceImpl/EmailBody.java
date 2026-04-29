package com.verve.guard.serviceImpl;

import com.verve.guard.notificationDTO.DepositNotification;
import com.verve.guard.notificationDTO.TransferNotification;
import com.verve.guard.notificationDTO.VerveNotification;
import com.verve.guard.notificationDTO.WithdrawalNotification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@Component
public class EmailBody {

    LocalDate localDate = LocalDate.now();
    String formattedDate = localDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG));

    LocalTime localTime = LocalTime.now();
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
    String formattedTime = timeFormatter.format(localTime);

    @Value("${spring.company.name}")
    private String companyName;

    public String adminEmailBody(VerveNotification verveNotification){

        String mailContent = "<p> Hi "+ verveNotification.adminFirstName()+ " "+ verveNotification.adminLastName()+ " </p>"+
                "<p>We noticed a suspicious endpoint call on transfer service, please here are the details "+"" +
                "<p> Merchant Name: "+verveNotification.fraudsterFirstName()+" "+verveNotification.fraudsterLastName()+""+"</p>" +
                "<p> Ip Address: "+verveNotification.ip_Address()+" </p>"+
                "<p> Device Type: "+verveNotification.deviceType()+" "+"</p>"+
                "<p> Browser: "+verveNotification.browser()+" "+"</p>"+
                "<p> Operating System: "+verveNotification.operatingSystem()+" </p>"+
                "<p> Merchant Email: "+verveNotification.fraudsterEmail()+" "+"</p>"+
                "<p> Merchant Phone: "+verveNotification.fraudsterPhone()+" </p>"+
                "<p> Date and Time: "+verveNotification.localDateTime()+" "+"</p>"+
                "<p> Thank you</p> <br> "+companyName+"";

        return mailContent;

    }

    public String depositCreditAlertEmailBody(DepositNotification depositNotification){

        String mailContent = "<p> Credit Alert On: "+ depositNotification.accountNumber() + " </p>"+
                "<p> Amount: "+"USD "+depositNotification.depositAmount()+" CR"+"</p>" +
                "<p> Reference: "+depositNotification.firstName()+" "+depositNotification.lastName()+""+"</p>" +
                "<p> Date & Time: "+formattedDate+"  "+formattedTime+"</p>"+
                "<p> Thank you</p> <br> "+companyName+"";

        return mailContent;

    }

    public String withdrawalDebitAlertEmailBody(WithdrawalNotification withdrawalNotification){

        String mailContent = "<p> Debit Alert On: "+ withdrawalNotification.accountNumber()+ " </p>"+
                "<p> Amount: "+"USD "+withdrawalNotification.withdrawalAmount()+" DR"+"</p>" +
                "<p> Reference: "+withdrawalNotification.firstName()+" "+withdrawalNotification.lastName()+""+"</p>" +
                "<p> Date & Time: "+formattedDate+"  "+formattedTime+"</p>"+
                "<p> Thank you</p> <br> "+companyName+"";

        return mailContent;

    }


    public String creditAlertEmailBody(TransferNotification transferNotification){

        String mailContent = "<p> Credit Alert On: "+ transferNotification.receiverAccountNumber() + " </p>"+
                "<p> Amount: "+"USD "+transferNotification.transferAmount()+" CR"+"</p>" +
                "<p> Sender: "+transferNotification.senderFirstName()+" "+transferNotification.senderLastName()+" </p>"+
                "<p> Date & Time: "+formattedDate+"  "+formattedTime+"</p>"+
                "<p> Thank you</p> <br> "+companyName+"";

        return mailContent;

    }


    public String debitAlertEmailBody(TransferNotification transferNotification){

        String mailContent = "<p> Debit Alert On: "+ transferNotification.senderAccountNumber() + " </p>"+
                "<p> Amount: "+"USD "+transferNotification.transferAmount()+" DR"+"</p>" +
                "<p> Recipient: "+transferNotification.receiverFirstName()+" "+transferNotification.receiverLastName()+""+"</p>" +
                "<p> Date & Time: "+formattedDate+"  "+formattedTime+"</p>"+
                "<p> Thank you</p> <br> "+companyName+"";

        return mailContent;

    }

}
