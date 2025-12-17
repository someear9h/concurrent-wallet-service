package com.wallet.model;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Wallet {
    private BigDecimal balance = BigDecimal.ZERO;

    // store transaction ids which are already been processed
    private final Set<String> processedKeys = ConcurrentHashMap.newKeySet();

    /**
     * ATOMIC UPDATE METHOD:
     * the 'synchronized' keyword is the most critical part
     * it ensures that only ONE thread can execute this method for this specific Wallet instance at a time
     * this prevents the "Lost Update" race condition
     * */
    public synchronized void credit(BigDecimal amount, String idempotencyKey) {
        if(processedKeys.contains(idempotencyKey)) {
            System.out.println("Duplicate Request ignored for the key: " + idempotencyKey);
            return;
        }

        // update balance
        this.balance = this.balance.add(amount);

        // key is processed
        this.processedKeys.add(idempotencyKey);
    }

    // synchronized reader to ensure we don't read a value while it's being updated
    public synchronized BigDecimal getBalance() {
        return balance;
    }
}
