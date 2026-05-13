package com.verve.guard.serviceImpl;

import com.verve.guard.entity.DeviceLog;
import com.verve.guard.entity.TransactionHistory;
import com.verve.guard.entity.User;
import com.verve.guard.enums.TransactionStatus;
import com.verve.guard.enums.TransactionType;
import com.verve.guard.exception.*;
import com.verve.guard.kafka.NotificationProducer;
import com.verve.guard.mapper.AccountManagementMapper;
import com.verve.guard.notificationDTO.TransferNotification;
import com.verve.guard.notificationDTO.VerveNotification;
import com.verve.guard.repository.AccountManagementRepository;
import com.verve.guard.repository.DeviceLogRepository;
import com.verve.guard.repository.TransactionHistoryRepository;
import com.verve.guard.repository.UserRepository;
import com.verve.guard.request.TransferRequest;
import com.verve.guard.response.DeviceResponse;
import com.verve.guard.response.TransactionResponse;
import com.verve.guard.service.TransferService;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
public class TransferServiceImpl implements TransferService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;
    private final TransactionHistoryRepository historyRepository;
    private final DeviceLogRepository deviceLogRepository;
    private final NotificationProducer notificationProducer;
    private final EntityManager entityManager;
    private final AccountManagementRepository managementRepository;
    private final AccountManagementMapper managementMapper;
    private final TransferUtility transferUtility;

    //
    private static final BigDecimal MAXIMUM_AMOUNT_THRESHOLD = new BigDecimal("5000000"); //5 MILLION NAIRA
    private static final int MAX_REQUESTS = 4;
    private static final int TIME_WINDOW_SECONDS = 10;


    public TransferServiceImpl(RedisTemplate<String, Object> redisTemplate, UserRepository userRepository, TransactionHistoryRepository historyRepository, DeviceLogRepository deviceLogRepository, NotificationProducer notificationProducer, EntityManager entityManager, AccountManagementRepository managementRepository, AccountManagementMapper managementMapper, TransferUtility transferUtility) {
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
        this.deviceLogRepository = deviceLogRepository;
        this.notificationProducer = notificationProducer;
        this.entityManager = entityManager;
        this.managementRepository = managementRepository;
        this.managementMapper = managementMapper;
        this.transferUtility = transferUtility;
    }


    @Override
    @Transactional
    public TransactionResponse transfer(TransferRequest transferRequest, String currency, String idempotencyKey, Authentication currentUser, HttpServletRequest httpRequest) {

        User user = (User) currentUser.getPrincipal();

        User userDetails = userRepository.findByAccountNumber(transferRequest.getAccountNumber()).orElseThrow(() -> new UserNotFoundException("Invalid account number"));

        User superUser = userRepository.findById(user.getId()).orElseThrow(() -> new AccountNumberNotFoundException("Account number not found"));

        // Fraud check first, before any lock (Idempotency)
        try {
            //SECURITY ======> FRAUD DETECTION ONE
            transferUtility.verveGuardOne(superUser, transferRequest, httpRequest);

            //SECURITY ======> FRAUD DETECTION ONE
            transferUtility.verveGuardTwo(superUser, httpRequest);
        } catch (SuspiciousActivityException suspiciousActivityException) {
            log.warn("FRAUD DETECTED — transfer blocked for user {}", user.getId());
            throw suspiciousActivityException; // rethrow — stops everything
        }

        String responseKey = "idem:transfer:" + idempotencyKey;
        String lockKey = responseKey + ":lock";

        Boolean lockAcquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "LOCK", Duration.ofSeconds(30));

        if (Boolean.FALSE.equals(lockAcquired)) {
            throw new DuplicateTransferRequestException(
                    "Duplicate request in progress. Please wait...");
        }


        /// //////////
        User userFound = YouCannotSendMoneyToYourself(superUser, transferRequest);
        log.info("Passed user conflict checks");

        //debit is done here
        //Debiting is done here
        Query queryCount = entityManager.createNativeQuery("SELECT COUNT(user_id) FROM account_management WHERE user_id =:user_id");

        queryCount.setParameter("user_id", userFound.getId());

        long UserIdCount = (Long) queryCount.getSingleResult();

        // Remember BODMAS RULE (Bracket-> Order-> Division-> Multiplication-> Addition-> Subtraction)
        // Procedure, we have Open-Bracket-Multiplication then Summation -> Division -> Multiplied by the User-DB-Count, then Subtraction from main account balance
        Query query  =  entityManager.createNativeQuery("UPDATE account_management SET balance = balance - "+ transferRequest.getAmount() +" / " + UserIdCount + " WHERE user_id =:user_id " );
        query.setParameter("user_id", user.getId());

        query.executeUpdate();


        //I'm handling crediting here
        var managementForReceiver  = userRepository.findByAccountNumber(transferRequest.getAccountNumber()).orElseThrow(() -> new UserNotFoundException("No user with this account number is found"));
        //
        var management = managementRepository.save(managementMapper.saveTransaction(managementForReceiver, transferRequest));

        log.info("Both sender and receiver transactions have been committed");

        //SENDING EMAIL ALERTS FOR SENDER AND RECEIVER
        sendTransferEmailAlert(userDetails, superUser, transferRequest);

        //
        TransactionHistory transaction = TransactionHistory.builder()
                .amount(transferRequest.getAmount())
                .currency(management.getCurrency())
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.PAYMENT_SUCCESSFUL)
                .build();

        historyRepository.save(transaction);

        //
        insufficientBalanceForTransfer(user, transferRequest);

        return TransactionResponse.builder()
                .amount(transferRequest.getAmount())
                .currency(transferRequest.getCurrency())
                .status(TransactionStatus.PAYMENT_SUCCESSFUL)
                .transactionType(TransactionType.TRANSFER)
                .build();
    }


    public User insufficientBalanceForTransfer(User user, TransferRequest transferRequest) {

        User userFound = userRepository.findById(user.getId()).orElseThrow(() -> new UserNotFoundException("User not found"));

        Query query  =  entityManager.createNativeQuery("SELECT SUM(balance) FROM account_management WHERE user_id =:user_id" );

        query.setParameter("user_id", userFound.getId());

        BigDecimal totalAmount = (BigDecimal) query.getSingleResult();

        //BigDecimal LIMIT_DEPOSIT_AMOUNT = new BigDecimal("100.00");
        if (totalAmount.doubleValue() <= transferRequest.getAmount().doubleValue()){
            throw new InsufficientBalanceException("Insufficient balance");
        }

        return userFound;
    }


    //
    public User YouCannotSendMoneyToYourself(User user, TransferRequest transferRequest) {

        User userFound = userRepository.findById(user.getId()).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (transferRequest.getAccountNumber().equals(userFound.getAccountNumber()))
        {
            throw new YouCannotSendMoneyToYourselfException("You Cannot Send Money To Yourself");
        }
        return userFound;

    }


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

    public DeviceResponse deviceInformation(@NonNull User user, User superUser, HttpServletRequest request) {

        String ipAddress = userIpAddress(request);

        String userAgentString = request.getHeader("User-Agent");

        UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);

        var deviceLog = DeviceLog.builder()
                .ip_Address(ipAddress)
                .localDateTime(LocalDateTime.now())
                .operatingSystem(userAgent.getOperatingSystem().getName())
                .deviceType(userAgent.getOperatingSystem().getDeviceType().getName())
                .browser(userAgent.getBrowser().getName())
                .fraudsterFirstName(user.getFirstName())
                .fraudsterLastName(user.getLastName())
                .fraudsterEmail(user.getEmail())
                .fraudsterPhone(user.getPhone())
                .build();

        log.info("Checking IP Address And Device Type: {} {}", request.getRemoteAddr(), deviceLog.getOperatingSystem());

        deviceLogRepository.save(deviceLog);

        sendFraudEmailAlert(ipAddress, userAgent, user, superUser);

        return DeviceResponse.builder()
                .ip_Address(ipAddress)
                .localDateTime(LocalDateTime.now())
                .operatingSystem(userAgent.getOperatingSystem().getName())
                .deviceType(userAgent.getOperatingSystem().getDeviceType().getName())
                .browser(userAgent.getBrowser().getName())
                .fraudsterFirstName(user.getFirstName())
                .fraudsterLastName(user.getLastName())
                .fraudsterEmail(user.getEmail())
                .fraudsterPhone(user.getPhone())
                .adminFirstName(superUser.getFirstName())
                .adminLastName(superUser.getLastName())
                .adminEmail(superUser.getEmail())
                .adminPhone(superUser.getPhone())
                .build();
    }

    //
    public void sendFraudEmailAlert(String ipAddress, UserAgent userAgent, User user, User superUser) {

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
    public void sendTransferEmailAlert(User userDetails, User superUser, TransferRequest transferRequest) {

        this.notificationProducer.sendTransferNotification(
                new TransferNotification(
                        superUser.getFirstName(),
                        superUser.getLastName(),
                        superUser.getEmail(),
                        superUser.getAccountNumber(),
                        userDetails.getFirstName(),
                        userDetails.getLastName(),
                        userDetails.getEmail(),
                        userDetails.getAccountNumber(),
                        transferRequest.getAmount()
                )
        );

    }

}
