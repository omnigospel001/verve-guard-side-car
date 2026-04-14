package com.verve.guard.response;

import com.verve.guard.enums.TransactionStatus;
import com.verve.guard.enums.TransactionType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private BigDecimal amount;
    private String currency;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    private TransactionStatus status;
    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

}
