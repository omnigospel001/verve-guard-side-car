package com.verve.guard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "device_log")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime localDateTime;
    private String fraudsterFirstName;
    private String fraudsterLastName;
    private String fraudsterPhone;
    private String fraudsterEmail;
    private String ip_Address;
    private String operatingSystem;
    private String browser;
    private String deviceType;

}
