package org.example.workhub.service.impl;

import org.example.workhub.exception.TooManyRequestsException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryChatRateLimitServiceTest {

    @Test
    void rejectsRequestsOverConfiguredLimit() {
        InMemoryChatRateLimitService service = new InMemoryChatRateLimitService();
        ReflectionTestUtils.setField(service, "maxRequests", 2);
        ReflectionTestUtils.setField(service, "windowSeconds", 60L);

        service.checkAllowed("candidate-1");
        service.checkAllowed("candidate-1");

        assertThatThrownBy(() -> service.checkAllowed("candidate-1"))
                .isInstanceOf(TooManyRequestsException.class);
    }
}

