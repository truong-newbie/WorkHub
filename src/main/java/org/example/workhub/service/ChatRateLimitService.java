package org.example.workhub.service;

public interface ChatRateLimitService {
    void checkAllowed(String candidateId);
}

