package com.verve.guard.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    private String currency;

    private String cardNumber;

    @NotNull(message = "Account Number is required")
    private Long accountNumber;

    @Positive(message = "Amount must be positive")
    @Min(value = 100, message = "Amount is too low")
    @NotNull(message = "Amount is required")
    private BigDecimal amount = BigDecimal.ZERO;

}
