package com.verve.guard.service;

import com.verve.guard.request.DepositRequest;
import com.verve.guard.response.TransactionResponse;
import org.springframework.security.core.Authentication;

public interface DepositService {

    TransactionResponse deposit(DepositRequest managementRequest, String idempotencyKey, Authentication currentUser);
}
