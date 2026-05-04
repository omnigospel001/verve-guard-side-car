package com.verve.guard.controller;

import com.verve.guard.request.DepositRequest;
import com.verve.guard.response.TransactionResponse;
import com.verve.guard.service.DepositService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class DepositController {

    private final DepositService depositService;

    public DepositController(DepositService depositService) {
        this.depositService = depositService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                                       @Valid @RequestBody DepositRequest depositRequest,
                                                       Authentication currentUser) {

        return ResponseEntity.ok().body(depositService.deposit(depositRequest, idempotencyKey, currentUser));
    }

}
