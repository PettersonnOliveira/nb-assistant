package br.com.petterson.nbassistant.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class ChatMetricsService {

    private final AtomicLong totalQuestions = new AtomicLong();

    public void recordQuestion() {
        totalQuestions.incrementAndGet();
    }

    public long getTotalQuestions() {
        return totalQuestions.get();
    }
}
