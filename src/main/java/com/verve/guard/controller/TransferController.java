package com.verve.guard.controller;

import com.verve.guard.request.TransferRequest;
import com.verve.guard.response.TransactionResponse;
import com.verve.guard.service.TransferService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                                        @Valid @RequestBody TransferRequest transferRequest,
                                                        Authentication currentUser,
                                                        HttpServletRequest httpRequest) {

        String currency = "NGN";
        return ResponseEntity.ok().body(transferService.transfer(transferRequest, currency, idempotencyKey, currentUser, httpRequest));
    }
}
