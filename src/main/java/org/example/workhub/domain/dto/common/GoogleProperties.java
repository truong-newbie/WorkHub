package org.example.workhub.domain.dto.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.google")
public class GoogleProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String userInfoUri;
}
