package com.fooddeliveryapp.api.services;

import com.fooddeliveryapp.api.exceptions.LockBusyException;
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
     * @param lockKey   the unique key for the lock (e.g., "order:restaurant:123")
     * @param waitTime  the maximum time to wait for acquiring the lock
     * @param leaseTime lock lease time (auto-released after this time as a safety net)
     * @param timeUnit  time unit for waitTime and leaseTime
     * @param action    the logic to execute while holding the lock
     * @param <T>       return type of the action
     * @return the result of the action
     * @throws LockBusyException if the lock cannot be acquired within waitTime
     * @throws LockBusyException if the thread is interrupted while waiting
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime,
                                 TimeUnit timeUnit, Supplier<T> action) {
        RLock lock = redissonClient.getLock("lock:" + lockKey);
        boolean isLocked = false;
        try {
            log.info("[DistributedLock] Acquiring lock for key: {}", lockKey);
            isLocked = lock.tryLock(waitTime, leaseTime, timeUnit);

            if (isLocked) {
                log.info("[DistributedLock] Lock acquired for key: {}", lockKey);
                return action.get();
            } else {
                log.warn("[DistributedLock] Could not acquire lock for key: {} after waiting {} {}",
                        lockKey, waitTime, timeUnit.name().toLowerCase());
                throw new LockBusyException(
                        "Hệ thống đang xử lý yêu cầu tương tự. Vui lòng thử lại sau vài giây.");
            }
        } catch (LockBusyException e) {
            throw e; // re-throw dedicated exception as-is
        } catch (InterruptedException e) {
            log.error("[DistributedLock] Thread interrupted while waiting for lock: {}", lockKey, e);
            Thread.currentThread().interrupt();
            throw new LockBusyException("Yêu cầu bị gián đoạn khi chờ xử lý. Vui lòng thử lại.", e);
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("[DistributedLock] Lock released for key: {}", lockKey);
            }
        }
    }
}
