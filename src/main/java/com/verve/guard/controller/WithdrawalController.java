package com.verve.guard.controller;

import com.verve.guard.request.WithdrawalRequest;
import com.verve.guard.response.TransactionResponse;
import com.verve.guard.service.WithdrawalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    public WithdrawalController(WithdrawalService withdrawalService) {
        this.withdrawalService = withdrawalService;
    }

    @PutMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdrawal(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                                          @Valid @RequestBody WithdrawalRequest withdrawalRequest,
                                                          Authentication currentUser) {

        return ResponseEntity.ok().body(withdrawalService.withdraw(withdrawalRequest, idempotencyKey, currentUser));
    }
}
