package com.verve.guard.service;

import com.verve.guard.entity.User;
import com.verve.guard.request.TransferRequest;
import com.verve.guard.response.TransactionResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

public interface TransferService {

    TransactionResponse transfer(TransferRequest transferRequest, String currency, String idempotencyKey, Authentication currentUser, HttpServletRequest request);

    void verveGuard(User user, User superUser, TransferRequest transferRequest, HttpServletRequest request);

}
