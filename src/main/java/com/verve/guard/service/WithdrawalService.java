package com.verve.guard.service;

import com.verve.guard.request.WithdrawalRequest;
import com.verve.guard.response.TransactionResponse;
import org.springframework.security.core.Authentication;

public interface WithdrawalService {

    TransactionResponse withdraw(WithdrawalRequest withdrawalRequest, String idempotencyKey, Authentication currentUser);

}
