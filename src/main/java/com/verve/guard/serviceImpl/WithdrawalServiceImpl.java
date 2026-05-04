package com.verve.guard.serviceImpl;

import com.verve.guard.entity.TransactionHistory;
import com.verve.guard.entity.User;
import com.verve.guard.enums.TransactionStatus;
import com.verve.guard.enums.TransactionType;
import com.verve.guard.exception.DuplicateTransferRequestException;
import com.verve.guard.exception.InsufficientBalanceException;
import com.verve.guard.exception.UserNotFoundException;
import com.verve.guard.kafka.NotificationProducer;
import com.verve.guard.mapper.AccountManagementMapper;
import com.verve.guard.notificationDTO.WithdrawalNotification;
import com.verve.guard.repository.TransactionHistoryRepository;
import com.verve.guard.repository.UserRepository;
import com.verve.guard.request.WithdrawalRequest;
import com.verve.guard.response.TransactionResponse;
import com.verve.guard.service.WithdrawalService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;

@Service
@Slf4j
public class WithdrawalServiceImpl implements WithdrawalService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;
    private final TransactionHistoryRepository historyRepository;
    private final EntityManager entityManager;
    private final NotificationProducer notificationProducer;


    public WithdrawalServiceImpl(RedisTemplate<String, Object> redisTemplate, UserRepository userRepository, TransactionHistoryRepository historyRepository, NotificationProducer notificationProducer, AccountManagementMapper managementMapper, EntityManager entityManager) {
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
        this.notificationProducer = notificationProducer;
        this.entityManager = entityManager;
    }



    @Override
    @Transactional
    public TransactionResponse withdraw(WithdrawalRequest withdrawalRequest, String idempotencyKey, Authentication currentUser) {

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

        Query queryCount = entityManager.createNativeQuery("SELECT COUNT(user_id) FROM account_management WHERE user_id =:user_id");

        queryCount.setParameter("user_id", userFound.getId());

        long UserIdCount = (Long) queryCount.getSingleResult();

        // Remember BODMAS RULE (Bracket-> Order-> Division-> Multiplication-> Addition-> Subtraction)
        // Procedure, we have Open-Bracket-Multiplication then Summation -> Division -> Multiplied by the User-DB-Count, then Subtraction from main account balance
        Query query  =  entityManager.createNativeQuery("UPDATE account_management SET balance = balance - "+ withdrawalRequest.getBalance() +" / " + UserIdCount + " WHERE user_id =:user_id " );

        query.setParameter("user_id", userFound.getId());

        query.executeUpdate();


        //I'm accounting for balance sufficiency
        insufficientBalanceForWithdrawal(user, withdrawalRequest);


        TransactionHistory transaction = TransactionHistory.builder()
                .amount(withdrawalRequest.getBalance())
                .currency(withdrawalRequest.getCurrency())
                .transactionType(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.PAYMENT_SUCCESSFUL)
                .build();

        historyRepository.save(transaction);

        //SENDING WITHDRAWAL EMAIL ALERT
        sendWithdrawalEmailAlert(userFound, withdrawalRequest);

        return TransactionResponse.builder()
                .amount(withdrawalRequest.getBalance())
                .currency(withdrawalRequest.getCurrency())
                .status(TransactionStatus.PAYMENT_SUCCESSFUL)
                .transactionType(TransactionType.WITHDRAWAL)
                .build();
    }

    public User insufficientBalanceForWithdrawal(User user, WithdrawalRequest withdrawalRequest) {

        User userFound = userRepository.findById(user.getId()).orElseThrow(() -> new UserNotFoundException("User not found"));

        Query query  =  entityManager.createNativeQuery("SELECT SUM(balance) FROM account_management WHERE user_id =:user_id" );

        query.setParameter("user_id", userFound.getId());

        BigDecimal totalAmount = (BigDecimal) query.getSingleResult();

        //BigDecimal LIMIT_DEPOSIT_AMOUNT = new BigDecimal("100.00");
        if (totalAmount.doubleValue() <= withdrawalRequest.getBalance().doubleValue()){
            throw new InsufficientBalanceException("Insufficient balance");
        }

        return userFound;
    }


    //
    public void sendWithdrawalEmailAlert(User superUser, WithdrawalRequest withdrawalRequest) {

        this.notificationProducer.sendWithdrawalNotification(
                new WithdrawalNotification(
                        superUser.getFirstName(),
                        superUser.getLastName(),
                        superUser.getEmail(),
                        superUser.getAccountNumber(),
                        withdrawalRequest.getBalance()
                )
        );

    }




}
