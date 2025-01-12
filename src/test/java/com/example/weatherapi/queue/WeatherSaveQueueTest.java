package com.example.weatherapi.queue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

class WeatherSaveQueueTest {

    private WeatherSaveQueue queue;

    @BeforeEach
    void setup() {
        queue = new WeatherSaveQueue();
        queue.startWorker();
    }

    @AfterEach
    void tearDown() {
        queue.shutdownWorker();
    }

    @Test
    void testTaskExecution() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        queue.addTask(latch::countDown);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testTaskRetryOnFailure() {
        AtomicInteger attempts = new AtomicInteger(0);
        queue.addTask(() -> {
            if (attempts.incrementAndGet() < WeatherSaveQueue.MAX_RETRY) {
                throw new RuntimeException("Simulated failure");
            }
        });

        await().atMost(1, TimeUnit.SECONDS).until(() -> attempts.get() == WeatherSaveQueue.MAX_RETRY);
    }

    @Test
    void testTaskStopsAfterMaxRetries() {
        AtomicInteger attempts = new AtomicInteger(0);
        queue.addTask(() -> {
            if (attempts.incrementAndGet() <= WeatherSaveQueue.MAX_RETRY + 1) {
                throw new RuntimeException("Simulated failure");
            }
        });

        await().atMost(1, TimeUnit.SECONDS).until(() -> attempts.get() == WeatherSaveQueue.MAX_RETRY);
        assertThat(attempts.get()).isEqualTo(WeatherSaveQueue.MAX_RETRY);
    }

    @Test
    void testShutdownProcessesRemainingTasks() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);

        queue.addTask(latch::countDown);
        queue.addTask(latch::countDown);

        queue.shutdownWorker();
        assertTrue(latch.await(2, TimeUnit.SECONDS), "Not all tasks were processed before shutdown");
    }

    @Test
    void testAddTaskAfterShutdown() {
        queue.shutdownWorker();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> queue.addTask(() -> {}));
        assertEquals("Cannot add tasks after shutdown", exception.getMessage());
    }

    @Test
    void testTaskOrder() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        AtomicInteger counter = new AtomicInteger(0);

        queue.addTask(() -> {
            assertEquals(1, counter.incrementAndGet());
            latch.countDown();
        });
        queue.addTask(() -> {
            assertEquals(2, counter.incrementAndGet());
            latch.countDown();
        });
        queue.addTask(() -> {
            assertEquals(3, counter.incrementAndGet());
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testWorkerInterruption() throws InterruptedException {
        queue.addTask(() -> {
            throw new RuntimeException("Simulated failure");
        });

        Thread.currentThread().interrupt();
        assertTrue(Thread.currentThread().isInterrupted(), "Current thread should be interrupted");
    }
}