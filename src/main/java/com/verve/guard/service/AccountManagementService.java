package com.verve.guard.service;

import com.verve.guard.entity.User;
import com.verve.guard.request.DepositRequest;
import com.verve.guard.request.TransferRequest;
import com.verve.guard.request.WithdrawalRequest;
import com.verve.guard.response.TransactionResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;

public interface AccountManagementService {

    TransactionResponse deposit(DepositRequest managementRequest, String idempotencyKey, Long id);

    TransactionResponse withdraw(WithdrawalRequest withdrawalRequest, String idempotencyKey, Long id);

    TransactionResponse transfer(TransferRequest transferRequest, String currency, String idempotencyKey, Long id, HttpServletRequest request);

    void verveGuard(User user, User superUser, TransferRequest transferRequest, HttpServletRequest request);

}
