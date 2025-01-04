package com.example.weatherapi.queue;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class WeatherSaveQueue {

    private static final Logger logger = LoggerFactory.getLogger(WeatherSaveQueue.class);

    private final BlockingQueue<SaveTask> taskQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @PostConstruct
    public void startWorker() {
        executorService.submit(() -> {
            while (true) {
                try {
                    SaveTask task = taskQueue.take();
                    task.execute();
                } catch (Exception e) {
                    logger.error("Failed to process save task", e);
                }
            }
        });
    }

    public void addTask(SaveTask task) {
        taskQueue.add(task);
    }

    @PreDestroy
    public void shutdownWorker() {
        executorService.shutdown();
    }

    public interface SaveTask {
        void execute();
    }
}
