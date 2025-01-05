package com.example.weatherapi.queue;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Component
public class WeatherSaveQueue {

    private static final Logger LOG = LoggerFactory.getLogger(WeatherSaveQueue.class);

    private static final int MAX_QUEUE_SIZE = 1000;
    private static final long RETRY_SLEEP_MS = 100;
    public static final int MAX_RETRY = 3;

    private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "SaveWorker");
        thread.setDaemon(true);
        return thread;
    });

    private volatile boolean running = true;


    @PostConstruct
    public void startWorker() {
        LOG.info("Starting SaveWorker");
        executorService.submit(() -> {
            while (running || !taskQueue.isEmpty()) {
                try {
                    Runnable task = taskQueue.poll(1, TimeUnit.SECONDS);
                    if (task != null) {
                        executeTaskWithRetry(task);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOG.warn("SaveWorker interrupted", e);
                } catch (Exception e) {
                    LOG.error("Unexpected error in SaveWorker", e);
                }
            }
        });
    }

    public void addTask(Runnable task) {
        if (!running) {
            throw new IllegalStateException("Cannot add tasks after shutdown");
        }
        if (!taskQueue.offer(task)) {
            LOG.error("Task queue is full! Task dropped.");
        }
    }

    @PreDestroy
    public void shutdownWorker() {
        running = false;
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                LOG.warn("Worker did not terminate within the timeout, forcing shutdown");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Shutdown interrupted", e);
        }
    }

    private void executeTaskWithRetry(Runnable task) {
        int attempt = 1;
        while (true) {
            try {
                task.run();
                return;
            } catch (Exception e) {
                LOG.error("Task execution failed on attempt {}: {}", attempt, task, e);
                if (attempt >= MAX_RETRY) {
                    LOG.error("Max retries reached for task: {}", task);
                    return;
                }
                attempt++;
                try {
                    TimeUnit.MILLISECONDS.sleep(RETRY_SLEEP_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    LOG.warn("Retry sleep interrupted", ie);
                    return;
                }
            }
        }
    }

}
