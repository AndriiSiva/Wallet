package com.wallet.app.controller;

import com.wallet.app.dto.WalletRequest;
import com.wallet.app.dto.WalletResponse;
import com.wallet.app.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/wallet")
    public Mono<Void> processWalletOperation(@Valid @RequestBody WalletRequest request) {
        return walletService.processOperation(request)
                .then(Mono.delay(Duration.ofMillis(500)))
                .then();
    }

    @GetMapping("/wallets/{walletId}")
    public Mono<WalletResponse> getWalletBalance(@PathVariable UUID walletId) {
        return walletService.getWallet(walletId)
                .map(wallet -> new WalletResponse(wallet.getId(), wallet.getBalance()));
    }
}