package com.wallet.app.dto;


import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WalletRequest {

    @NotNull(message = "Wallet ID cannot be null")
    private UUID walletId;

    @NotNull(message = "Operation type cannot be null")
    private OperationType OperationType;

    @NotNull(message = "Amount cannot be null")
    private BigDecimal amount;



}
