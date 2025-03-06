package com.wallet.app.service;

import com.wallet.app.dto.WalletRequest;
import com.wallet.app.entity.Wallet;
import com.wallet.app.exception.InsufficientFundsException;
import com.wallet.app.exception.WalletNotFoundException;
import com.wallet.app.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final Sinks.Many<WalletRequest> requestSink = Sinks.many().multicast().onBackpressureBuffer();

    {
        requestSink.asFlux()
                .publishOn(Schedulers.boundedElastic())
                .flatMap(this::processOperationInternal, 1)
                .doOnError(throwable ->
                        System.err.println("Error processing request: " + throwable.getMessage()))
                .subscribe();
    }

    public Mono<Void> processOperation(WalletRequest request) {
        return processOperationInternal(request);
    }

    private Mono<Void> processOperationInternal(WalletRequest request) {
        UUID walletId = request.getWalletId();
        return walletRepository.findByIdForUpdate(walletId)
                .defaultIfEmpty(new Wallet(walletId, BigDecimal.ZERO, 0L))
                .flatMap(wallet -> {
                    BigDecimal amount = request.getAmount();
                    BigDecimal newBalance;
                    switch (request.getOperationType()) {
                        case DEPOSIT:
                            newBalance = wallet.getBalance().add(amount);
                            break;
                        case WITHDRAW:
                            newBalance = wallet.getBalance().subtract(amount);
                            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                                return Mono.error(new InsufficientFundsException("Insufficient funds for wallet: " + walletId));
                            }
                            break;
                        default:
                            return Mono.error(new IllegalArgumentException("Unknown operation type"));
                    }
                    return walletRepository.upsertBalance(walletId, newBalance)
                            .timeout(Duration.ofMillis(50))
                            .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
                            .then();
                });
    }

    public Mono<Wallet> getWallet(UUID walletId) {
        return walletRepository.findById(walletId)
                .switchIfEmpty(Mono.error(new WalletNotFoundException("Wallet not found: " + walletId)));
    }
}