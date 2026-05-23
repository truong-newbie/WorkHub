package org.example.workhub.service;

import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.request.SubscriberCreateRequest;
import org.example.workhub.domain.dto.request.SubscriberSearchRequest;
import org.example.workhub.domain.dto.request.SubscriberUpdateRequest;
import org.example.workhub.domain.dto.response.SubscriberMailResponse;
import org.example.workhub.domain.dto.response.SubscriberResponse;
import org.example.workhub.domain.dto.response.SubscriberUnsubscribeResponse;

public interface SubscriberService {

    SubscriberResponse createSubscriber(SubscriberCreateRequest request);

    SubscriberResponse updateSubscriber(Long id, SubscriberUpdateRequest request);

    SubscriberResponse deleteSubscriber(Long id);

    SubscriberResponse getSubscriberDetail(Long id);

    SubscriberResponse getCurrentUserSubscriber();

    SubscriberResponse enableSubscriber(Long id);

    SubscriberResponse disableSubscriber(Long id);

    PaginationResponseDto<SubscriberResponse> getSubscribers(SubscriberSearchRequest request);

    SubscriberMailResponse sendMatchingJobEmails();

    SubscriberUnsubscribeResponse unsubscribeByToken(String token);
}
