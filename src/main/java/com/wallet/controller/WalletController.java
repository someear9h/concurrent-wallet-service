package com.wallet.controller;

import com.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@Tag(name = "Wallet API", description = "Operations for top-ups and rewards")
public class WalletController {
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    // DTO classes (for JSON body)
    public static class TopUpRequest {
        public String userId;
        public BigDecimal amountUSD;  // 1 dollar = 1 coin
        public String idempotencyKey;
    }

    public static class RewardRequest {
        public String userId;
        public BigDecimal amountCoins;
        public String idempotencyKey;
    }

    @PostMapping("/wallet/topup")
    @Operation(summary = "Top Up Wallet", description = "Converts USD to Coins (1:1)")
    public Map<String, Object> topup(@RequestBody TopUpRequest request) {
        walletService.processTransaction(request.userId, request.amountUSD, request.idempotencyKey);

        return Map.of(
                "status", "success",
                "newBalance", walletService.getBalance(request.userId)
        );
    }

    @PostMapping("/game/reward")
    @Operation(summary = "Credit Game Reward", description = "Credits won coins to the user wallet")
    public Map<String, Object> reward(@RequestBody RewardRequest request) {
        walletService.processTransaction(request.userId, request.amountCoins, request.idempotencyKey);

        return Map.of(
                "status", "success",
                "newBalance", walletService.getBalance(request.userId)
        );
    }

    @GetMapping("/wallet/{userId}")
    @Operation(summary = "Get Balance", description = "Returns current wallet balance")
    public Map<String, Object> getBalance(@PathVariable String userId) {
        return Map.of(
                "userId", userId,
                "balance", walletService.getBalance(userId)
        );
    }
}
