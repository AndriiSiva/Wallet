package com.wallet.app.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class WalletResponse {

    private UUID walletId;

    private BigDecimal balance;
    

}
