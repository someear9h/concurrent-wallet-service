# Run Instructions and Explanation Paragraph

## 1. Run Instructions (README.md content)

**How to Run the Server**
The application is containerized for consistent execution.

1. Open your terminal in the project root.
2. Run the following command to build and start the service:
```bash
docker compose up --build

```


3. The API will start on port `8080`. You can access the **Swagger UI** to interact with the endpoints manually:
* [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)


**How to Run the Concurrency Test**
I have included a JUnit test (`WalletConcurrencyTest.java`) that simulates multi-threaded race conditions to verify the solution.

1. **Using Docker:** The tests run automatically during the build phase.
2. **Using Maven (Locally):**
```bash
./mvnw test

```


* Look for `testConcurrentTopupAndReward`: This spawns simultaneous threads to ensure the final balance is mathematically correct (proving atomicity).

---

## 2. Design & Concurrency Explanation
I avoided the 'lost update' race condition—where concurrent threads read a stale balance before writing—by encapsulating the balance within a Wallet object and applying the synchronized keyword to its state-mutating methods. This acts as a monitor lock, forcing concurrent requests for the same user to execute sequentially rather than in parallel, ensuring atomic updates. For idempotency, I maintained a thread-safe Set of processed transaction IDs within the critical section itself. This approach handles edge cases like aggressive network retries effectively; if a duplicate request arrives while the first is still processing or after it finishes, the system detects the existing key within the lock and ignores the duplicate operation, guaranteeing exactly-once semantics.
