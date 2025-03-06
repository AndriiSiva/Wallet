package com.wallet.app;

import com.wallet.app.dto.OperationType;
import com.wallet.app.dto.WalletRequest;
import com.wallet.app.dto.WalletResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
public class WalletControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("wallet_db")
            .withUsername("postgres")
            .withPassword("password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/wallet_db");
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
        registry.add("spring.liquibase.url", () -> "jdbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/wallet_db");
        registry.add("spring.liquibase.user", postgres::getUsername);
        registry.add("spring.liquibase.password", postgres::getPassword);
    }

    @Autowired
    private WebTestClient webTestClient;

    private UUID walletId;

    @BeforeAll
    static void beforeAll() {
        postgres.start();

        Mono.delay(Duration.ofSeconds(2)).block();
    }

    @BeforeEach
    void setUp() {
        walletId = UUID.randomUUID();
    }

    @Test
    void testSuccessfulDeposit() {
        WalletRequest request = new WalletRequest();
        request.setWalletId(walletId);
        request.setOperationType(OperationType.DEPOSIT);
        request.setAmount(BigDecimal.valueOf(1000));

        webTestClient.post().uri("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();

        webTestClient.get().uri("/api/v1/wallets/{walletId}", walletId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(WalletResponse.class)
                .consumeWith(response -> {
                    WalletResponse walletResponse = response.getResponseBody();
                    assert walletResponse != null;
                    assert walletResponse.getWalletId().equals(walletId);
                    assert walletResponse.getBalance().compareTo(BigDecimal.valueOf(1000)) == 0;
                });
    }

    @Test
    void testSuccessfulWithdraw() {
        WalletRequest depositRequest = new WalletRequest();
        depositRequest.setWalletId(walletId);
        depositRequest.setOperationType(OperationType.DEPOSIT);
        depositRequest.setAmount(BigDecimal.valueOf(2000));

        webTestClient.post().uri("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(depositRequest)
                .exchange()
                .expectStatus().isOk();

        WalletRequest withdrawRequest = new WalletRequest();
        withdrawRequest.setWalletId(walletId);
        withdrawRequest.setOperationType(OperationType.WITHDRAW);
        withdrawRequest.setAmount(BigDecimal.valueOf(1000));

        webTestClient.post().uri("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(withdrawRequest)
                .exchange()
                .expectStatus().isOk();

        webTestClient.get().uri("/api/v1/wallets/{walletId}", walletId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(WalletResponse.class)
                .consumeWith(response -> {
                    WalletResponse walletResponse = response.getResponseBody();
                    assert walletResponse != null;
                    assert walletResponse.getWalletId().equals(walletId);
                    assert walletResponse.getBalance().compareTo(BigDecimal.valueOf(1000)) == 0;
                });
    }

    @Test
    void testWithdrawInsufficientFunds() {
        WalletRequest request = new WalletRequest();
        request.setWalletId(walletId);
        request.setOperationType(OperationType.WITHDRAW);
        request.setAmount(BigDecimal.valueOf(1000));

        webTestClient.post().uri("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Insufficient funds for wallet: " + walletId);
    }

    @Test
    void testInvalidJson() {
        webTestClient.post().uri("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{invalid_json}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Invalid JSON");
    }

    @Test
    void testGetExistingWallet() {
        WalletRequest request = new WalletRequest();
        request.setWalletId(walletId);
        request.setOperationType(OperationType.DEPOSIT);
        request.setAmount(BigDecimal.valueOf(500));

        webTestClient.post().uri("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();

        webTestClient.get().uri("/api/v1/wallets/{walletId}", walletId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(WalletResponse.class)
                .consumeWith(response -> {
                    WalletResponse walletResponse = response.getResponseBody();
                    assert walletResponse != null;
                    assert walletResponse.getWalletId().equals(walletId);
                    assert walletResponse.getBalance().compareTo(BigDecimal.valueOf(500)) == 0;
                });
    }

    @Test
    void testGetNonExistingWallet() {
        UUID nonExistingWalletId = UUID.randomUUID();
        webTestClient.get().uri("/api/v1/wallets/{walletId}", nonExistingWalletId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Wallet not found: " + nonExistingWalletId);
    }
}