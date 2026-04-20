package com.fooddeliveryapp.api.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockService {

    private final RedissonClient redissonClient;

    /**
     * Executes the given supplier logic within a distributed lock.
     *
     * @param lockKey    the unique key for the lock (e.g., "order:123")
     * @param waitTime   the maximum time to acquire the lock
     * @param leaseTime  lock lease time (automatically unlocked after this time)
     * @param timeUnit   time unit for waitTime and leaseTime
     * @param action     the logic to execute
     * @param <T>        return type of the action
     * @return the result of the action
     * @throws RuntimeException if the lock cannot be acquired or Thread is interrupted
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, Supplier<T> action) {
        RLock lock = redissonClient.getLock("lock:" + lockKey);
        boolean isLocked = false;
        try {
            log.info("Attempting to acquire lock for key: {}", lockKey);
            isLocked = lock.tryLock(waitTime, leaseTime, timeUnit);
            
            if (isLocked) {
                log.info("Successfully acquired lock for key: {}", lockKey);
                return action.get();
            } else {
                log.warn("Failed to acquire lock for key: {}", lockKey);
                throw new RuntimeException("Resource is currently busy. Please try again later.");
            }
        } catch (InterruptedException e) {
            log.error("Thread was interrupted while waiting for lock: {}", lockKey, e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while waiting for lock", e);
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("Successfully released lock for key: {}", lockKey);
            }
        }
    }
}
