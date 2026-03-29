package org.example.workhub.domain.dto.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.facebook")
public class FacebookProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;

}
