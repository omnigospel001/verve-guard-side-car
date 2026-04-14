package com.verve.guard.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceResponse {

    private String fraudsterFirstName;
    private String fraudsterLastName;
    private String fraudsterPhone;
    private String fraudsterEmail;
    private String ip_Address;
    private String operatingSystem;
    private String browser;
    private String deviceType;

    //
    private String adminFirstName;
    private String adminLastName;
    private String adminPhone;
    private String adminEmail;

    private LocalDateTime localDateTime;
}
