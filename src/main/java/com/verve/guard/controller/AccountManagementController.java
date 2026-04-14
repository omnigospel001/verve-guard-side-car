package com.verve.guard.controller;

import com.verve.guard.request.DepositRequest;
import com.verve.guard.request.TransferRequest;
import com.verve.guard.request.WithdrawalRequest;
import com.verve.guard.response.TransactionResponse;
import com.verve.guard.service.AccountManagementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1")
public class AccountManagementController {

    private final AccountManagementService accountManagementService;

    public AccountManagementController(AccountManagementService accountManagementService) {
        this.accountManagementService = accountManagementService;
    }

    @PostMapping("/transfer/{id}")
    public ResponseEntity<TransactionResponse> transfer(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                                        @Valid @RequestBody TransferRequest transferRequest,
                                                        @PathVariable Long id,
                                                        HttpServletRequest httpRequest) {

        String currency = "NGN";
        return ResponseEntity.ok().body(accountManagementService.transfer(transferRequest, currency, idempotencyKey, id, httpRequest));
    }

    @PostMapping("/deposit/{id}")
    public ResponseEntity<TransactionResponse> deposit(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                                        @Valid @RequestBody DepositRequest depositRequest,
                                                        @PathVariable Long id) {

        return ResponseEntity.ok().body(accountManagementService.deposit(depositRequest, idempotencyKey, id));
    }

    @PutMapping("/withdraw/{id}")
    public ResponseEntity<TransactionResponse> withdrawal(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                                       @Valid @RequestBody WithdrawalRequest withdrawalRequest,
                                                       @PathVariable Long id) {

        return ResponseEntity.ok().body(accountManagementService.withdraw(withdrawalRequest, idempotencyKey, id));
    }

}
