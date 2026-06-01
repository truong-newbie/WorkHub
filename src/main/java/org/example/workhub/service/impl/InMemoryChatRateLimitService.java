package org.example.workhub.service.impl;

import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.exception.TooManyRequestsException;
import org.example.workhub.service.ChatRateLimitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryChatRateLimitService implements ChatRateLimitService {

    @Value("${chat.rate-limit.max-requests:20}")
    private int maxRequests;

    @Value("${chat.rate-limit.window-seconds:60}")
    private long windowSeconds;

    private final Map<String, Deque<Instant>> requestsByCandidate = new ConcurrentHashMap<>();

    @Override
    public void checkAllowed(String candidateId) {
        Instant cutoff = Instant.now().minusSeconds(windowSeconds);
        Deque<Instant> requests = requestsByCandidate.computeIfAbsent(candidateId, ignored -> new ArrayDeque<>());
        synchronized (requests) {
            while (!requests.isEmpty() && requests.peekFirst().isBefore(cutoff)) {
                requests.removeFirst();
            }
            if (requests.size() >= maxRequests) {
                throw new TooManyRequestsException(ErrorMessage.Chat.ERR_RATE_LIMIT);
            }
            requests.addLast(Instant.now());
        }
    }
}

