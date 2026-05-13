package com.verve.guard.serviceImpl;

import com.verve.guard.entity.AccountManagement;
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
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
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
class TransferServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionHistoryRepository historyRepository;

    @Mock
    private DeviceLogRepository deviceLogRepository;

    @Mock
    private NotificationProducer notificationProducer;

    @Mock
    private EntityManager entityManager;

    @Mock
    private AccountManagementRepository managementRepository;

    @Mock
    private AccountManagementMapper managementMapper;

    @Mock
    private TransferUtility transferUtility;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private Query query;

    @Mock
    private Query queryCount;

    @InjectMocks
    private TransferServiceImpl transferService;

    private User sender;
    private User receiver;
    private TransferRequest transferRequest;
    private AccountManagement accountManagement;

    @BeforeEach
    void setUp() {

        sender = new User();
        sender.setId(1L);
        sender.setFirstName("John");
        sender.setLastName("Doe");
        sender.setEmail("john@gmail.com");
        sender.setPhone("08012345678");
        sender.setAccountNumber(1111111111L);

        receiver = new User();
        receiver.setId(2L);
        receiver.setFirstName("Jane");
        receiver.setLastName("Smith");
        receiver.setEmail("jane@gmail.com");
        receiver.setPhone("09087654321");
        receiver.setAccountNumber(2222222222L);

        transferRequest = new TransferRequest();
        transferRequest.setAccountNumber(2222222222L);
        transferRequest.setAmount(BigDecimal.valueOf(5000));
        transferRequest.setCurrency("NGN");

        accountManagement = new AccountManagement();
        accountManagement.setCurrency("NGN");
        accountManagement.setBalance(BigDecimal.valueOf(5000));
    }

    @Test
    void transfer_success() {

        when(authentication.getPrincipal()).thenReturn(sender);

        when(userRepository.findByAccountNumber(2222222222L))
                .thenReturn(Optional.of(receiver));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(sender));

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.setIfAbsent(
                anyString(),
                eq("LOCK"),
                any(Duration.class)
        )).thenReturn(true);

        when(entityManager.createNativeQuery(
                contains("SELECT COUNT")
        )).thenReturn(queryCount);

        when(queryCount.setParameter(anyString(), any()))
                .thenReturn(queryCount);

        when(queryCount.getSingleResult())
                .thenReturn(1L);

        when(entityManager.createNativeQuery(
                startsWith("UPDATE")
        )).thenReturn(query);

        when(query.setParameter(anyString(), any()))
                .thenReturn(query);

        when(query.executeUpdate())
                .thenReturn(1);

        when(managementMapper.saveTransaction(receiver, transferRequest))
                .thenReturn(accountManagement);

        when(managementRepository.save(any(AccountManagement.class)))
                .thenReturn(accountManagement);

        Query balanceQuery = mock(Query.class);

        when(entityManager.createNativeQuery(
                startsWith("SELECT SUM")
        )).thenReturn(balanceQuery);

        when(balanceQuery.setParameter(anyString(), any()))
                .thenReturn(balanceQuery);

        when(balanceQuery.getSingleResult())
                .thenReturn(BigDecimal.valueOf(100000));

        TransactionResponse response =
                transferService.transfer(
                        transferRequest,
                        "NGN",
                        "idem-key",
                        authentication,
                        httpServletRequest
                );

        assertNotNull(response);

        assertEquals(
                TransactionType.TRANSFER,
                response.getTransactionType()
        );

        assertEquals(
                TransactionStatus.PAYMENT_SUCCESSFUL,
                response.getStatus()
        );

        verify(historyRepository)
                .save(any());

        verify(notificationProducer)
                .sendTransferNotification(any(TransferNotification.class));
    }

    @Test
    void transfer_duplicateRequest_throwsException() {

        when(authentication.getPrincipal()).thenReturn(sender);

        when(userRepository.findByAccountNumber(anyLong()))
                .thenReturn(Optional.of(receiver));

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(sender));

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.setIfAbsent(
                anyString(),
                eq("LOCK"),
                any(Duration.class)
        )).thenReturn(false);

        assertThrows(
                DuplicateTransferRequestException.class,
                () -> transferService.transfer(
                        transferRequest,
                        "NGN",
                        "idem-key",
                        authentication,
                        httpServletRequest
                )
        );
    }

    @Test
    void transfer_receiverNotFound_throwsException() {

        when(authentication.getPrincipal()).thenReturn(sender);

        when(userRepository.findByAccountNumber(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> transferService.transfer(
                        transferRequest,
                        "NGN",
                        "idem-key",
                        authentication,
                        httpServletRequest
                )
        );
    }

    @Test
    void transfer_senderNotFound_throwsException() {

        when(authentication.getPrincipal()).thenReturn(sender);

        when(userRepository.findByAccountNumber(anyLong()))
                .thenReturn(Optional.of(receiver));

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNumberNotFoundException.class,
                () -> transferService.transfer(
                        transferRequest,
                        "NGN",
                        "idem-key",
                        authentication,
                        httpServletRequest
                )
        );
    }

    @Test
    void insufficientBalanceForTransfer_success() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(sender));

        Query balanceQuery = mock(Query.class);

        when(entityManager.createNativeQuery(anyString()))
                .thenReturn(balanceQuery);

        when(balanceQuery.setParameter(anyString(), any()))
                .thenReturn(balanceQuery);

        when(balanceQuery.getSingleResult())
                .thenReturn(BigDecimal.valueOf(100000));

        User result =
                transferService.insufficientBalanceForTransfer(
                        sender,
                        transferRequest
                );

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void insufficientBalanceForTransfer_throwsException() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(sender));

        Query balanceQuery = mock(Query.class);

        when(entityManager.createNativeQuery(anyString()))
                .thenReturn(balanceQuery);

        when(balanceQuery.setParameter(anyString(), any()))
                .thenReturn(balanceQuery);

        when(balanceQuery.getSingleResult())
                .thenReturn(BigDecimal.valueOf(1000));

        assertThrows(
                InsufficientBalanceException.class,
                () -> transferService.insufficientBalanceForTransfer(
                        sender,
                        transferRequest
                )
        );
    }

    @Test
    void youCannotSendMoneyToYourself_throwsException() {

        transferRequest.setAccountNumber(1111111111L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(sender));

        assertThrows(
                YouCannotSendMoneyToYourselfException.class,
                () -> transferService.YouCannotSendMoneyToYourself(
                        sender,
                        transferRequest
                )
        );
    }

    @Test
    void userIpAddress_fromHeader() {

        when(httpServletRequest.getHeader("X-Forwarded-For"))
                .thenReturn("192.168.1.1");

        String ip =
                transferService.userIpAddress(httpServletRequest);

        assertEquals("192.168.1.1", ip);
    }

    @Test
    void userIpAddress_fromRemoteAddress() {

        when(httpServletRequest.getHeader("X-Forwarded-For"))
                .thenReturn(null);

        when(httpServletRequest.getRemoteAddr())
                .thenReturn("127.0.0.1");

        String ip =
                transferService.userIpAddress(httpServletRequest);

        assertEquals("127.0.0.1", ip);
    }

    @Test
    void deviceInformation_success() {

        when(httpServletRequest.getHeader("X-Forwarded-For"))
                .thenReturn("127.0.0.1");

        when(httpServletRequest.getHeader("User-Agent"))
                .thenReturn(
                        "Mozilla/5.0 Windows NT 10.0 Win64 x64"
                );

        DeviceResponse response =
                transferService.deviceInformation(
                        receiver,
                        sender,
                        httpServletRequest
                );

        assertNotNull(response);

        verify(deviceLogRepository)
                .save(any());

        verify(notificationProducer)
                .sendNotification(any(VerveNotification.class));
    }

    @Test
    void sendFraudEmailAlert_success() {

        UserAgent userAgent =
                UserAgent.parseUserAgentString(
                        "Mozilla/5.0 Windows NT 10.0 Win64 x64"
                );

        transferService.sendFraudEmailAlert(
                "127.0.0.1",
                userAgent,
                receiver,
                sender
        );

        verify(notificationProducer)
                .sendNotification(any(VerveNotification.class));
    }

    @Test
    void sendTransferEmailAlert_success() {

        transferService.sendTransferEmailAlert(
                receiver,
                sender,
                transferRequest
        );

        verify(notificationProducer)
                .sendTransferNotification(
                        any(TransferNotification.class)
                );
    }
}