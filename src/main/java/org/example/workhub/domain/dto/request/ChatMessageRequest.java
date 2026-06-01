package org.example.workhub.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequest {

    private Long conversationId;

    @NotBlank(message = "{chat.message.required}")
    @Size(max = 1000, message = "{chat.message.too.long}")
    private String message;
}

