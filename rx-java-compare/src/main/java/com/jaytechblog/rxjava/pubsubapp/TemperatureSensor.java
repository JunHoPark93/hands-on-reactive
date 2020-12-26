package com.jaytechblog.rxjava.pubsubapp;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

@Component
public class TemperatureSensor {
    private final ApplicationEventPublisher publisher;
    private final Random random = new Random();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public TemperatureSensor(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostConstruct
    public void startProcessing() {
        this.executorService.schedule(this::probe, 1, SECONDS);
    }

    private void probe() {
        double temperature = 16 + random.nextGaussian() * 10;
        publisher.publishEvent(new Temperature(temperature));
        // 0~5초 랜덤 시간 지난 후 다음 스케줄 예약
        executorService.schedule(this::probe, random.nextInt(5000), MILLISECONDS);
    }
}

