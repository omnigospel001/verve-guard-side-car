package com.verve.guard.mapper;

import com.verve.guard.entity.AccountManagement;
import com.verve.guard.entity.User;
import com.verve.guard.enums.TransactionType;
import com.verve.guard.exception.RequestCannotBeNullException;
import com.verve.guard.exception.UserNotFoundException;
import com.verve.guard.repository.AccountManagementRepository;
import com.verve.guard.request.TransferRequest;
import org.springframework.stereotype.Component;

@Component
public class AccountManagementMapper {

    private final AccountManagementRepository managementRepository;

    public AccountManagementMapper(AccountManagementRepository managementRepository) {
        this.managementRepository = managementRepository;
    }

    public  AccountManagement saveTransaction(User user, TransferRequest transferRequest) {

        var managementForSender  = managementRepository.findById(user.getId()).orElseThrow(() -> new UserNotFoundException("User not really found"));

        try {
            if (transferRequest == null) throw new RequestCannotBeNullException("Request cannot be empty");

            return AccountManagement.builder()
                    .currency(transferRequest.getCurrency())
                    .balance(transferRequest.getAmount())
                    .transactionType(TransactionType.TRANSFER)
                    .cardNumber(managementForSender.getCardNumber())
                    .merchantId(user.getMerchantId())
                    .user(user)
                    .build();

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

    }
}
