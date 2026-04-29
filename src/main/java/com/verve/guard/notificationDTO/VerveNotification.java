package com.verve.guard.notificationDTO;

import java.time.LocalDateTime;

public record VerveNotification(
        String fraudsterFirstName,
        String fraudsterLastName,
        String fraudsterPhone,
        String fraudsterEmail,
        String ip_Address,
        String operatingSystem,
        String browser,
        String deviceType,

        //
        String adminFirstName,
        String adminLastName,
        String adminPhone,
        String adminEmail,

        LocalDateTime localDateTime
) {
}
