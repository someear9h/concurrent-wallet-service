package com.wallet.service;

import com.wallet.model.Wallet;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WalletService {

    // in memory db: concurrent hashmap
    private final Map<String, Wallet> walletStore = new ConcurrentHashMap<>();

    public void processTransaction(String userId, BigDecimal amount, String idempotencyKey) {
        if(amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        // get the wallet of user if it exists, or make new wallet if does not exists
        // computeIfAbsent is atomic
        Wallet userWallet = walletStore.computeIfAbsent(userId, k -> new Wallet());

        //the thread-safety logic to the Wallet object itself
        userWallet.credit(amount, idempotencyKey);
    }

    public BigDecimal getBalance(String userId) {
        // ff wallet doesn't exist, return 0
        Wallet userWallet = walletStore.get(userId);
        return (userWallet != null) ? userWallet.getBalance() : BigDecimal.ZERO;
    }
}
