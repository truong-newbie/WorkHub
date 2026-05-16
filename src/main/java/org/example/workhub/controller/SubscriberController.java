package org.example.workhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.example.workhub.base.RestApiV1;
import org.example.workhub.base.VsResponseUtil;
import org.example.workhub.constant.UrlConstant;
import org.example.workhub.domain.dto.request.SubscriberCreateRequest;
import org.example.workhub.domain.dto.request.SubscriberSearchRequest;
import org.example.workhub.domain.dto.request.SubscriberUpdateRequest;
import org.example.workhub.service.SubscriberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@RestApiV1
@Tag(name = "Subscriber Controller", description = "APIs for job email subscriptions")
public class SubscriberController {

    SubscriberService subscriberService;

    @Operation(summary = "Create subscriber", description = "Candidate subscribes to job emails by skills")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Subscriber created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Subscriber email already exists")
    })
    @PostMapping(UrlConstant.Subscriber.SUBSCRIBER_BASE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createSubscriber(@RequestBody @Valid SubscriberCreateRequest request) {
        return VsResponseUtil.success(HttpStatus.CREATED, subscriberService.createSubscriber(request));
    }

    @Operation(summary = "Update subscriber", description = "Update current user's subscriber or admin-managed subscriber")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscriber updated successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Subscriber not found")
    })
    @PutMapping(UrlConstant.Subscriber.ID)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateSubscriber(
            @PathVariable @Parameter(description = "Subscriber ID") Long id,
            @RequestBody @Valid SubscriberUpdateRequest request) {
        return VsResponseUtil.success(subscriberService.updateSubscriber(id, request));
    }

    @Operation(summary = "Delete subscriber", description = "Soft delete subscriber")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscriber deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Subscriber not found")
    })
    @DeleteMapping(UrlConstant.Subscriber.ID)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteSubscriber(@PathVariable @Parameter(description = "Subscriber ID") Long id) {
        return VsResponseUtil.success(subscriberService.deleteSubscriber(id));
    }

    @Operation(summary = "Get subscriber detail", description = "Get subscriber detail by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscriber returned"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Subscriber not found")
    })
    @GetMapping(UrlConstant.Subscriber.ID)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getSubscriberDetail(@PathVariable @Parameter(description = "Subscriber ID") Long id) {
        return VsResponseUtil.success(subscriberService.getSubscriberDetail(id));
    }

    @Operation(summary = "Get current user subscriber", description = "Get current user's subscriber config")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscriber returned"),
            @ApiResponse(responseCode = "404", description = "Subscriber not found")
    })
    @GetMapping(UrlConstant.Subscriber.ME)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUserSubscriber() {
        return VsResponseUtil.success(subscriberService.getCurrentUserSubscriber());
    }

    @Operation(summary = "Enable subscriber", description = "Enable subscriber email notifications")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscriber enabled successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Subscriber not found")
    })
    @PutMapping(UrlConstant.Subscriber.ENABLE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> enableSubscriber(@PathVariable @Parameter(description = "Subscriber ID") Long id) {
        return VsResponseUtil.success(subscriberService.enableSubscriber(id));
    }

    @Operation(summary = "Disable subscriber", description = "Disable subscriber email notifications")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscriber disabled successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Subscriber not found")
    })
    @PutMapping(UrlConstant.Subscriber.DISABLE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> disableSubscriber(@PathVariable @Parameter(description = "Subscriber ID") Long id) {
        return VsResponseUtil.success(subscriberService.disableSubscriber(id));
    }

    @Operation(summary = "Search subscribers", description = "Admin or recruiter searches subscribers with pagination and filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscribers returned"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping(UrlConstant.Subscriber.SUBSCRIBER_BASE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECRUITER')")
    public ResponseEntity<?> getSubscribers(@ModelAttribute SubscriberSearchRequest request) {
        return VsResponseUtil.success(subscriberService.getSubscribers(request));
    }

    @Operation(summary = "Send matching job emails", description = "Admin or recruiter manually triggers matching job emails")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Matching job emails sent"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping(UrlConstant.Subscriber.SEND_MAIL)
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECRUITER')")
    public ResponseEntity<?> sendMatchingJobEmails() {
        return VsResponseUtil.success(subscriberService.sendMatchingJobEmails());
    }
}
