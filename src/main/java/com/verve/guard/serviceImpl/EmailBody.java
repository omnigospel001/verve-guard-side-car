package com.verve.guard.serviceImpl;

import com.verve.guard.response.DeviceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailBody {

    @Value("${spring.company.name}")
    private String companyName;

    public String adminEmailBody(DeviceResponse deviceResponse){

        String mailContent = "<p> Hi "+ deviceResponse.getAdminFirstName()+ " "+ deviceResponse.getAdminLastName()+ " </p>"+
                "<p>We noticed a suspicious endpoint call on transfer service, please here are the details "+"" +
                "<p> Merchant Name: "+deviceResponse.getAdminFirstName()+" "+deviceResponse.getFraudsterLastName()+""+"</p>" +
                "<p> Ip Address: "+deviceResponse.getIp_Address()+" </p>"+
                "<p> Device Type: "+deviceResponse.getDeviceType()+" "+"</p>"+
                "<p> Ip Address: "+deviceResponse.getIp_Address()+" </p>"+
                "<p> Browser: "+deviceResponse.getBrowser()+" "+"</p>"+
                "<p> Operating System: "+deviceResponse.getOperatingSystem()+" </p>"+
                "<p> Merchant Email: "+deviceResponse.getFraudsterEmail()+" "+"</p>"+
                "<p> Merchant Phone: "+deviceResponse.getFraudsterPhone()+" </p>"+
                "<p> Date and Time: "+deviceResponse.getLocalDateTime()+" "+"</p>"+
                "<p> Thank you</p> <br> "+companyName+"";

        return mailContent;

    }
}
