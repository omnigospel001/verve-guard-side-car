package com.verve.guard.serviceImpl;

import com.verve.guard.entity.DeviceLog;
import com.verve.guard.entity.User;
import com.verve.guard.exception.ExcessAmountTransferException;
import com.verve.guard.exception.SuspiciousActivityException;
import com.verve.guard.kafka.NotificationProducer;
import com.verve.guard.notificationDTO.VerveNotification;
import com.verve.guard.repository.DeviceLogRepository;
import com.verve.guard.request.TransferRequest;
import com.verve.guard.response.DeviceResponse;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Slf4j
public class TransferUtility {

    private final DeviceLogRepository deviceLogRepository;
    private final NotificationProducer notificationProducer;
    private final RedisTemplate<String, Object> redisTemplate;
    private final DeviceLogPersistenceService deviceLogPersistenceService;


    //
    private static final BigDecimal MAXIMUM_AMOUNT_THRESHOLD = new BigDecimal("5000000"); //5 MILLION NAIRA
    private static final int MAX_REQUESTS = 5;
    private static final int TIME_WINDOW_SECONDS = 5;



    public TransferUtility(DeviceLogRepository deviceLogRepository, NotificationProducer notificationProducer, RedisTemplate<String, Object> redisTemplate, DeviceLogPersistenceService deviceLogPersistenceService) {
        this.deviceLogRepository = deviceLogRepository;
        this.notificationProducer = notificationProducer;
        this.redisTemplate = redisTemplate;
        this.deviceLogPersistenceService = deviceLogPersistenceService;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void verveGuardOne(User superUser, TransferRequest transferRequest, HttpServletRequest request) {

        if (transferRequest.getAmount().doubleValue() > MAXIMUM_AMOUNT_THRESHOLD.doubleValue())
        {
            deviceInformation(superUser, request);
            throw new ExcessAmountTransferException("FRAUD DETECTED: Transfer amount exceeds threshold: your account is LOCKED for 10 seconds ");
        }
    }





    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void verveGuardTwo(User superUser, HttpServletRequest request) {

        boolean userFraud = isUserRequestSuspicious(superUser);
        boolean ipFraud = isIpRequestSuspicious(request);

        log.info("LOG userFraud =============================> {}", userFraud);

        log.info("LOG ipFraud =============================> {}", ipFraud);

        if (userFraud || ipFraud) {

            log.info("LOG BEFORE FRAUD =============================>");

            deviceInformation(superUser, request);

            log.info("LOG AFTER FRAUD =============================>");

            throw new SuspiciousActivityException(
                    "Suspicious activity detected. You are BLOCKED for the moment"
            );
        }
    }


    private long incrementWithWindow(String key) {
        Long count = redisTemplate.opsForValue().increment(key);

        if (count == null) {
            throw new IllegalStateException("Redis increment failed because key is null: " + key);
        }

        if (count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(TIME_WINDOW_SECONDS));
        }

        return count;
    }


    private boolean isUserRequestSuspicious(User user) {
        String key = "fraud:transfer:user:" + user.getId();
        long count = incrementWithWindow(key);
        return count > MAX_REQUESTS;
    }

    private boolean isIpRequestSuspicious(HttpServletRequest request) {
        String key = "fraud:transfer:ip:" + userIpAddress(request);
        long count = incrementWithWindow(key);
        return count > MAX_REQUESTS;
    }


    //
    public DeviceResponse deviceInformation(User superUser, HttpServletRequest request) {

        String ipAddress = userIpAddress(request);

        String userAgentString = request.getHeader("User-Agent");

        UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);

        var deviceLog = DeviceLog.builder()
                .ip_Address(ipAddress)
                .localDateTime(LocalDateTime.now())
                .operatingSystem(userAgent.getOperatingSystem().getName())
                .deviceType(userAgent.getOperatingSystem().getDeviceType().getName())
                .browser(userAgent.getBrowser().getName())
                .fraudsterFirstName(superUser.getFirstName())
                .fraudsterLastName(superUser.getLastName())
                .fraudsterEmail(superUser.getEmail())
                .fraudsterPhone(superUser.getPhone())
                .build();

        log.info("Checking IP Address And Device Type: {} {}", request.getRemoteAddr(), deviceLog.getOperatingSystem());

        // Use the new service so this save commits independently
        deviceLogPersistenceService.saveDeviceLog(deviceLog);


        sendFraudEmailAlert(ipAddress, userAgent, superUser);

        return DeviceResponse.builder()
                .ip_Address(ipAddress)
                .localDateTime(LocalDateTime.now())
                .operatingSystem(userAgent.getOperatingSystem().getName())
                .deviceType(userAgent.getOperatingSystem().getDeviceType().getName())
                .browser(userAgent.getBrowser().getName())
                .adminFirstName(superUser.getFirstName())
                .adminLastName(superUser.getLastName())
                .adminEmail(superUser.getEmail())
                .adminPhone(superUser.getPhone())
                .build();
    }


    //
    public void sendFraudEmailAlert(String ipAddress, UserAgent userAgent, User superUser) {

        this.notificationProducer.sendNotification(
                new VerveNotification(
                        ipAddress,
                        userAgent.getOperatingSystem().getName(),
                        userAgent.getBrowser().getName(),
                        userAgent.getOperatingSystem().getDeviceType().getName(),
                        superUser.getFirstName(),
                        superUser.getLastName(),
                        superUser.getPhone(),
                        superUser.getEmail(),
                        LocalDateTime.now()
                )
        );

    }


    //
    public String userIpAddress(HttpServletRequest request) {

        String header = request.getHeader("X-Forwarded-For");

        if (header != null && !header.isEmpty() && !"unknown".equalsIgnoreCase(header)) {
            return header.split(",")[0];
        }

        String ipAddress = request.getRemoteAddr();

        //I'm converting IPv6 localhost to IPv4
        if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
            return "127.0.0.1";
        }

        return ipAddress;
    }

}

