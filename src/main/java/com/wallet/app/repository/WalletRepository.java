package com.wallet.app.repository;


import com.wallet.app.entity.Wallet;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;


import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface WalletRepository extends ReactiveCrudRepository<Wallet, UUID> {

   @Query("SELECT * FROM wallets WHERE id = :id FOR UPDATE")
    Mono<Wallet> findByIdForUpdate(UUID id);

    @Query("INSERT INTO wallets (id, balance, version) VALUES (:id, :balance, 1) " +
            "ON CONFLICT (id) DO UPDATE SET balance = :balance, version = wallets.version + 1")
    Mono<Integer> upsertBalance(UUID id, BigDecimal balance);
}