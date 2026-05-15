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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private AccountManagementRepository managementRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionHistoryRepository historyRepository;

    @Mock
    private NotificationProducer notificationProducer;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DepositServiceImpl depositService;

    private User user;
    private DepositRequest depositRequest;
    private AccountManagement accountManagement;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@gmail.com");
        user.setAccountNumber(1234567890L);
        user.setMerchantId("MERCHANT_001");

        depositRequest = new DepositRequest();
        depositRequest.setBalance(BigDecimal.valueOf(5000));
        depositRequest.setCurrency("NGN");

        accountManagement = new AccountManagement();
        accountManagement.setId(1L);
        accountManagement.setCardNumber(1234567812345678L);
        accountManagement.setUser(user);
    }

    @Test
    void deposit_success() {

        when(authentication.getPrincipal()).thenReturn(user);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(valueOperations.setIfAbsent(
                anyString(),
                eq("LOCK"),
                any(Duration.class)
        )).thenReturn(true);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(managementRepository.findById(1L))
                .thenReturn(Optional.of(accountManagement));

        when(managementRepository.save(any(AccountManagement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(historyRepository.save(any(TransactionHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response =
                depositService.deposit(
                        depositRequest,
                        "idem-key",
                        authentication
                );

        assertNotNull(response);

        assertEquals(
                TransactionType.DEPOSIT,
                response.getTransactionType()
        );

        assertEquals(
                TransactionStatus.PAYMENT_SUCCESSFUL,
                response.getStatus()
        );

        assertEquals(
                BigDecimal.valueOf(5000),
                response.getAmount()
        );

        verify(userRepository).findById(1L);

        verify(managementRepository)
                .findById(1L);

        verify(managementRepository)
                .save(any(AccountManagement.class));

        verify(historyRepository)
                .save(any(TransactionHistory.class));

        verify(notificationProducer)
                .sendDepositNotification(any(DepositNotification.class));
    }

    @Test
    void deposit_duplicateRequest_throwsException() {

        when(authentication.getPrincipal()).thenReturn(user);

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.setIfAbsent(
                anyString(),
                eq("LOCK"),
                any(Duration.class)
        )).thenReturn(false);

        assertThrows(
                DuplicateTransferRequestException.class,
                () -> depositService.deposit(
                        depositRequest,
                        "idem-key",
                        authentication
                )
        );

        verify(userRepository, never())
                .findById(anyLong());
    }

    @Test
    void deposit_userNotFound_throwsException() {

        when(authentication.getPrincipal()).thenReturn(user);

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.setIfAbsent(
                anyString(),
                eq("LOCK"),
                any(Duration.class)
        )).thenReturn(true);

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> depositService.deposit(
                        depositRequest,
                        "idem-key",
                        authentication
                )
        );

        verify(userRepository).findById(1L);
    }

    @Test
    void deposit_accountManagementNotFound_throwsException() {

        when(authentication.getPrincipal()).thenReturn(user);

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.setIfAbsent(
                anyString(),
                eq("LOCK"),
                any(Duration.class)
        )).thenReturn(true);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(managementRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> depositService.deposit(
                        depositRequest,
                        "idem-key",
                        authentication
                )
        );

        verify(managementRepository).findById(1L);
    }

    @Test
    void sendDepositEmailAlert_success() {

        depositService.sendDepositEmailAlert(
                user,
                depositRequest
        );

        ArgumentCaptor<DepositNotification> captor =
                ArgumentCaptor.forClass(DepositNotification.class);

        verify(notificationProducer)
                .sendDepositNotification(captor.capture());

        DepositNotification notification = captor.getValue();

        assertEquals(
                "John",
                notification.firstName()
        );

        assertEquals(
                "Doe",
                notification.lastName()
        );

        assertEquals(
                "john@gmail.com",
                notification.userEmail()
        );

        assertEquals(
                1234567890L,
                notification.accountNumber()
        );

        assertEquals(
                BigDecimal.valueOf(5000),
                notification.depositAmount()
        );
    }
}