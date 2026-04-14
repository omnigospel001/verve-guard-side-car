package com.verve.guard.serviceImpl;

import com.verve.guard.entity.AccountManagement;
import com.verve.guard.entity.DeviceLog;
import com.verve.guard.entity.TransactionHistory;
import com.verve.guard.entity.User;
import com.verve.guard.enums.TransactionStatus;
import com.verve.guard.enums.TransactionType;
import com.verve.guard.exception.*;
import com.verve.guard.kafka.NotificationProducer;
import com.verve.guard.kafka.VerveNotificationRequest;
import com.verve.guard.mapper.AccountManagementMapper;
import com.verve.guard.repository.AccountManagementRepository;
import com.verve.guard.repository.DeviceLogRepository;
import com.verve.guard.repository.TransactionHistoryRepository;
import com.verve.guard.repository.UserRepository;
import com.verve.guard.request.DepositRequest;
import com.verve.guard.request.TransferRequest;
import com.verve.guard.request.WithdrawalRequest;
import com.verve.guard.response.DeviceResponse;
import com.verve.guard.response.TransactionResponse;
import com.verve.guard.service.AccountManagementService;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
public class AccountManagementServiceImpl implements AccountManagementService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final AccountManagementRepository managementRepository;
    private final UserRepository userRepository;
    private final TransactionHistoryRepository historyRepository;
    private final DeviceLogRepository deviceLogRepository;
    private final NotificationProducer notificationProducer;
    private final AccountManagementMapper managementMapper;
    private final EntityManager entityManager;


    //
    private static final BigDecimal MINIMUM_DEPOSIT = new BigDecimal("100");
    private static final BigDecimal MAXIMUM_AMOUNT_THRESHOLD = new BigDecimal("5000000"); //5 MILLION NAIRA
    private static final int MAX_REQUESTS = 5;
    private static final int TIME_WINDOW_SECONDS = 10;


    public AccountManagementServiceImpl(RedisTemplate<String, Object> redisTemplate, AccountManagementRepository managementRepository, UserRepository userRepository, TransactionHistoryRepository historyRepository, DeviceLogRepository deviceLogRepository, NotificationProducer notificationProducer, AccountManagementMapper managementMapper, EntityManager entityManager) {
        this.redisTemplate = redisTemplate;
        this.managementRepository = managementRepository;
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
        this.deviceLogRepository = deviceLogRepository;
        this.notificationProducer = notificationProducer;
        this.managementMapper = managementMapper;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public TransactionResponse deposit(DepositRequest depositRequest, String idempotencyKey, Long id) {

        String responseKey = "idem:transfer:" + idempotencyKey;
        String lockKey = responseKey + ":lock";

        TransactionResponse cached =
                (TransactionResponse) redisTemplate.opsForValue().get(responseKey);

        if (cached != null) {
            log.info("Returning cached response for key={}", idempotencyKey);
            return cached;
        }

        Boolean lockAcquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "LOCK", Duration.ofSeconds(30));

        if (Boolean.FALSE.equals(lockAcquired)) {
            throw new DuplicateTransferRequestException(
                    "Duplicate request in progress. Please wait...");
        }

        //
//        try {
            User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Employee not found"));

            var managementForSender  = managementRepository.findById(user.getId()).orElseThrow(() -> new UserNotFoundException("User not really found"));

            //
            var management = new AccountManagement();
            management.setBalance(depositRequest.getBalance());
            management.setCurrency(depositRequest.getCurrency());
            management.setTransactionType(TransactionType.DEPOSIT);
            management.setCardNumber(managementForSender.getCardNumber());
            management.setMerchantId(user.getMerchantId());
            management.setUser(user);
            managementRepository.save(management);


            TransactionHistory transaction = TransactionHistory.builder()
                    .amount(depositRequest.getBalance())
                    .currency(management.getCurrency())
                    .transactionType(TransactionType.DEPOSIT)
                    .status(TransactionStatus.PAYMENT_SUCCESSFUL)
                    .build();

            historyRepository.save(transaction);

            var transactionResponse = TransactionResponse.builder()
                    .amount(depositRequest.getBalance())
                    .currency(depositRequest.getCurrency())
                    .status(TransactionStatus.PAYMENT_SUCCESSFUL)
                    .transactionType(TransactionType.DEPOSIT)
                    .build();

//            redisTemplate.opsForValue()
//                    .set(responseKey, transactionResponse, Duration.ofMinutes(0));
//
            return transactionResponse;
//        } finally
//    {
//        redisTemplate.delete(lockKey);
//    }
}

    @Override
    @Transactional
    public TransactionResponse withdraw(WithdrawalRequest withdrawalRequest, String idempotencyKey, Long id) {

        String responseKey = "idem:transfer:" + idempotencyKey;
        String lockKey = responseKey + ":lock";

        TransactionResponse cached =
                (TransactionResponse) redisTemplate.opsForValue().get(responseKey);

        if (cached != null) {
            log.info("Returning cached response for key={}", idempotencyKey);
            return cached;
        }

        Boolean lockAcquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "LOCK", Duration.ofSeconds(30));

        if (Boolean.FALSE.equals(lockAcquired)) {
            throw new DuplicateTransferRequestException(
                    "Duplicate request in progress. Please wait...");
        }

        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Employee not found"));

        var management = new AccountManagement();
        //I am handling withdrawal here
        management.setBalance(management.getBalance().subtract(withdrawalRequest.getBalance()));
        management.setCurrency(withdrawalRequest.getCurrency());
        management.setTransactionType(TransactionType.WITHDRAWAL);
        management.setUser(user);
        managementRepository.save(management);


        TransactionHistory transaction = TransactionHistory.builder()
                .amount(withdrawalRequest.getBalance())
                .currency(management.getCurrency())
                .transactionType(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.PAYMENT_SUCCESSFUL)
                .build();

        historyRepository.save(transaction);

        return TransactionResponse.builder()
                .amount(withdrawalRequest.getBalance())
                .currency(withdrawalRequest.getCurrency())
                .status(TransactionStatus.PAYMENT_SUCCESSFUL)
                .transactionType(TransactionType.WITHDRAWAL)
                .build();
    }

    @Override
    @Transactional
    public TransactionResponse transfer(TransferRequest transferRequest, String currency, String idempotencyKey, Long id, HttpServletRequest httpRequest) {

        //


        String responseKey = "idem:transfer:" + idempotencyKey;
        String lockKey = responseKey + ":lock";

        TransactionResponse cached =
                (TransactionResponse) redisTemplate.opsForValue().get(responseKey);

        if (cached != null) {
            log.info("Returning cached response for key={}", idempotencyKey);
            return cached;
        }

        Boolean lockAcquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "LOCK", Duration.ofSeconds(30));

        if (Boolean.FALSE.equals(lockAcquired)) {
            throw new DuplicateTransferRequestException(
                    "Duplicate request in progress. Please wait...");
        }


        User user = userRepository.findByAccountNumber(transferRequest.getAccountNumber()).orElseThrow(() -> new UserNotFoundException("Invalid account number"));

        User superUser = userRepository.findById(id).orElseThrow(() -> new AccountNumberNotFoundException("Account number not found"));

        //SECURITY
        verveGuard(user, superUser, transferRequest, httpRequest);

        //
        if (user.getAccountNumber().equals(superUser.getAccountNumber())) {
            log.info("Merchant or User is trying to transfer money to himself");
            throw new CannotSendMoneyToYourselfException("You cannot transfer money to yourself, but you can make a deposit");
        }
        log.info("Passed user conflict checks");

        //Checking if the user balance is sufficient for the transaction
        Query query = entityManager.createNativeQuery("SELECT SUM(balance) FROM account_management WHERE user_id =:user_id");
        query.setParameter("user_id", id);

        BigDecimal totalAmount = (BigDecimal) query.getSingleResult();

        if (totalAmount.doubleValue() <= transferRequest.getAmount().doubleValue()){

            throw new InsufficientBalanceException("Insufficient balance");
        }


        //
//        if (transferRequest.getAmount().doubleValue() <= MINIMUM_DEPOSIT.doubleValue())
//        {
//            throw new LowerAmountException("Amount must be greater than 100");
//        }
        log.info("Passed Amount checks");


        //debit is done here
        var managementForSender  = managementRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
        managementForSender.setBalance(managementForSender.getBalance().subtract(transferRequest.getAmount()));
        managementRepository.save(managementForSender);
        log.info("Passed Debiting");

        //I'm handling crediting here
        var managementForReceiver  = userRepository.findByAccountNumber(transferRequest.getAccountNumber()).orElseThrow(() -> new UserNotFoundException("No user with this account number is found"));

        //
        var management = managementRepository.save(managementMapper.saveTransaction(managementForReceiver, transferRequest));

        log.info("Both sender and receiver transactions have been committed");

        //
        TransactionHistory transaction = TransactionHistory.builder()
                .amount(transferRequest.getAmount())
                .currency(management.getCurrency())
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.PAYMENT_SUCCESSFUL)
                .build();

        historyRepository.save(transaction);

        return TransactionResponse.builder()
                .amount(transferRequest.getAmount())
                .currency(transferRequest.getCurrency())
                .status(TransactionStatus.PAYMENT_SUCCESSFUL)
                .transactionType(TransactionType.TRANSFER)
                .build();
    }


    @Override
    public void verveGuard(User user, User superUser, TransferRequest transferRequest, HttpServletRequest request) {

       //
       if (transferRequest.getAmount().doubleValue() > MAXIMUM_AMOUNT_THRESHOLD.doubleValue()){
           deviceInformation(user, superUser, request);
       }

        //
        detectMultipleRequests(user, superUser, request);
        //
        detectMultipleRequestsByIp(user, superUser, request);

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

    public DeviceResponse deviceInformation(User user, User superUser, HttpServletRequest request) {

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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void detectMultipleRequests(User user, User superUser, HttpServletRequest request) {

        String key = "fraud:transfer:user:" + user.getId();

        Long count = redisTemplate.opsForValue().increment(key);

        log.info("INCREMENT COUNT CHECK IN USER ID: {} ", count);


        if (count == null) return;

        // first request → set expiry window
        if (count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(TIME_WINDOW_SECONDS));
        }

        log.info("User {} has made {} requests within {} seconds", user.getEmail(), count, TIME_WINDOW_SECONDS);

        if (count > MAX_REQUESTS) {

            //Calling the device information email
            deviceInformation(user, superUser, request);

            throw new SuspiciousActivityException(
                    "Too many requests detected. Please wait and try again."
            );
        }
    }


    //
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void detectMultipleRequestsByIp(User user, User superUser, HttpServletRequest request) {

        //I'm getting the user ipAddress
        String ipAddress = userIpAddress(request);

        String key = "fraud:transfer:ip:" + ipAddress;

        Long count = redisTemplate.opsForValue().increment(key);

        log.info("INCREMENT COUNT CHECK IP ADDRESS: {} ", count);

        if (count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(10));
        }

        if (count > 20) {

            //Calling the device information email
            deviceInformation(user, superUser, request);

            throw new SuspiciousActivityException("Too many requests from this IP");
        }
    }

    //
    public void sendFraudEmailAlert(String ipAddress, UserAgent userAgent, User user, User superUser) {

        this.notificationProducer.sendNotification(
                new VerveNotificationRequest(
                        user.getFirstName(),
                        user.getLastName(),
                        user.getPhone(),
                        user.getEmail(),
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


}
