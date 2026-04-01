package org.example.workhub.domain.dto.common;

import lombok.Builder;

@Builder
public record MailBody(String to , String subject, String text) {
}
