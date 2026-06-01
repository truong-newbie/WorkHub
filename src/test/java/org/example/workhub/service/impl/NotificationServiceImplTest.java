package org.example.workhub.service.impl;

import org.example.workhub.constant.NotificationTargetType;
import org.example.workhub.constant.NotificationType;
import org.example.workhub.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationServiceImplTest {

    @Test
    void createAndSendStartsNewTransactionForAfterCommitListeners() throws NoSuchMethodException {
        Method method = NotificationServiceImpl.class.getMethod(
                "createAndSend",
                User.class,
                User.class,
                NotificationType.class,
                String.class,
                String.class,
                NotificationTargetType.class,
                String.class
        );

        Transactional transactional = method.getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
        assertThat(transactional.propagation()).isEqualTo(Propagation.REQUIRES_NEW);
    }
}
