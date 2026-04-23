package com.verve.guard.service;

import com.verve.guard.entity.User;
import com.verve.guard.request.DepositRequest;
import com.verve.guard.request.TransferRequest;
import com.verve.guard.request.WithdrawalRequest;
import com.verve.guard.response.TransactionResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;

public interface AccountManagementService {

    TransactionResponse deposit(DepositRequest managementRequest, String idempotencyKey, Authentication currentUser);

    TransactionResponse withdraw(WithdrawalRequest withdrawalRequest, String idempotencyKey, Authentication currentUser);

    TransactionResponse transfer(TransferRequest transferRequest, String currency, String idempotencyKey, Authentication currentUser, HttpServletRequest request);

    void verveGuard(User user, User superUser, TransferRequest transferRequest, HttpServletRequest request);

}
