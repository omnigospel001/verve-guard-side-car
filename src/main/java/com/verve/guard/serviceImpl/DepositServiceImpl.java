package com.verve.guard.serviceImpl;

import com.verve.guard.entity.AccountManagement;
import com.verve.guard.entity.TransactionHistory;
import com.verve.guard.entity.User;
import com.verve.guard.enums.TransactionStatus;
import com.verve.guard.enums.TransactionType;
import com.verve.guard.exception.DuplicateTransferRequestException;
import com.verve.guard.exception.UserNotFoundException;
import com.verve.guard.kafka.NotificationProducer;
import com.verve.guard.notificationDTO.DepositNotification;
import com.verve.guard.repository.AccountManagementRepository;
import com.verve.guard.repository.TransactionHistoryRepository;
import com.verve.guard.repository.UserRepository;
import com.verve.guard.request.DepositRequest;
import com.verve.guard.response.TransactionResponse;
import com.verve.guard.service.DepositService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
public class DepositServiceImpl implements DepositService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final AccountManagementRepository managementRepository;
    private final UserRepository userRepository;
    private final TransactionHistoryRepository historyRepository;
    private final NotificationProducer notificationProducer;


    public DepositServiceImpl(RedisTemplate<String, Object> redisTemplate, AccountManagementRepository managementRepository, UserRepository userRepository, TransactionHistoryRepository historyRepository, NotificationProducer notificationProducer) {
        this.redisTemplate = redisTemplate;
        this.managementRepository = managementRepository;
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
        this.notificationProducer = notificationProducer;
    }


    @Override
    @Transactional
    public TransactionResponse deposit(DepositRequest depositRequest, String idempotencyKey, Authentication currentUser) {

        User user = (User) currentUser.getPrincipal();

        String responseKey = "idem:transfer:" + idempotencyKey;
        String lockKey = responseKey + ":lock";

        Boolean lockAcquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "LOCK", Duration.ofSeconds(30));

        if (Boolean.FALSE.equals(lockAcquired)) {
            throw new DuplicateTransferRequestException(
                    "Duplicate request in progress. Please wait...");
        }

        User userFound = userRepository.findById(user.getId()).orElseThrow(() -> new UserNotFoundException("Employee not found"));

        var managementForSender  = managementRepository.findById(userFound.getId()).orElseThrow(() -> new UserNotFoundException("User not really found"));

        //
        var management = new AccountManagement();
        management.setBalance(depositRequest.getBalance());
        management.setCurrency(depositRequest.getCurrency());
        management.setTransactionType(TransactionType.DEPOSIT);
        management.setCardNumber(managementForSender.getCardNumber());
        management.setMerchantId(userFound.getMerchantId());
        management.setUser(userFound);
        managementRepository.save(management);


        TransactionHistory transaction = TransactionHistory.builder()
                .amount(depositRequest.getBalance())
                .currency(management.getCurrency())
                .transactionType(TransactionType.DEPOSIT)
                .status(TransactionStatus.PAYMENT_SUCCESSFUL)
                .build();

        historyRepository.save(transaction);

        //SENDING DEPOSIT EMAIL ALERT
        sendDepositEmailAlert(userFound, depositRequest);

        return TransactionResponse.builder()
                .amount(depositRequest.getBalance())
                .currency(depositRequest.getCurrency())
                .status(TransactionStatus.PAYMENT_SUCCESSFUL)
                .transactionType(TransactionType.DEPOSIT)
                .build();
    }

    //
    public void sendDepositEmailAlert(User superUser, DepositRequest depositRequest) {

        this.notificationProducer.sendDepositNotification(
                new DepositNotification(
                        superUser.getFirstName(),
                        superUser.getLastName(),
                        superUser.getEmail(),
                        superUser.getAccountNumber(),
                        depositRequest.getBalance()
                )
        );

    }

}
