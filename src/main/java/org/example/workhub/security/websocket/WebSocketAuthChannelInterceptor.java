package org.example.workhub.security.websocket;

import lombok.RequiredArgsConstructor;
import org.example.workhub.security.UserPrincipal;
import org.example.workhub.security.jwt.JwtTokenProvider;
import org.example.workhub.service.CustomUserDetailsService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            if (accessor.getUser() != null) {
                return message;
            }
            String token = resolveToken(accessor);
            if (!StringUtils.hasText(token) || !jwtTokenProvider.validateToken(token)) {
                throw new AccessDeniedException("Invalid WebSocket token");
            }
            String userId = jwtTokenProvider.extractSubjectFromJwt(token);
            UserPrincipal userPrincipal = (UserPrincipal) customUserDetailsService.loadUserById(userId);
            accessor.setUser(new WebSocketUserPrincipal(userPrincipal.getId()));
        }
        return message;
    }

    private String resolveToken(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        List<String> tokens = accessor.getNativeHeader("token");
        return tokens == null || tokens.isEmpty() ? null : tokens.get(0);
    }
}
