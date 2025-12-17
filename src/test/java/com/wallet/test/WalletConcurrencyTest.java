package com.wallet.test;

import com.wallet.service.WalletService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class WalletConcurrencyTest {
    @Autowired
    private WalletService walletService;

    @Test
    void testConcurrentTopupAndReward() throws InterruptedException {
        String userId = "user_test_concurrent";

        // We want to fire 2 threads at the EXACT same time
        int numberOfThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // Thread 1: Top Up $100
        executor.submit(() -> {
            try {
                walletService.processTransaction(userId, new BigDecimal("100"), "txn_1");
            } finally {
                latch.countDown();
            }
        });

        // Thread 2: Reward 50 Coins
        executor.submit(() -> {
            try {
                walletService.processTransaction(userId, new BigDecimal("50"), "txn_2");
            } finally {
                latch.countDown();
            }
        });

        // Wait for both threads to finish
        latch.await();

        // Assert: 100 + 50 must equal 150.
        // If race conditions existed, this might be 100 or 50.
        BigDecimal finalBalance = walletService.getBalance(userId);
        Assertions.assertEquals(new BigDecimal("150"), finalBalance);

        System.out.println("Test Passed! Final Balance: " + finalBalance);
    }

    @Test
    void testIdempotency() {
        String userId = "user_idempotent";
        String txnId = "unique_key_123";

        // First request
        walletService.processTransaction(userId, new BigDecimal("10"), txnId);

        // Duplicate request (Retry)
        walletService.processTransaction(userId, new BigDecimal("10"), txnId);

        // Balance should be 10, not 20
        Assertions.assertEquals(new BigDecimal("10"), walletService.getBalance(userId));
    }
}
