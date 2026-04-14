package com.verve.guard.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequest {

    private String currency;

    @Positive(message = "Amount must be positive")
    @Min(value = 100, message = "Amount is too low")
    @Max(value = 500_000_000, message = "Amount is too high")
    @NotNull(message = "Amount is required")
    private BigDecimal balance = BigDecimal.ZERO;
}
