package org.example.workhub.util;

import jakarta.servlet.http.HttpServletRequest;
import org.example.workhub.constant.CommonConstant;
import org.example.workhub.domain.entity.TokenBlacklist;
import org.example.workhub.repository.TokenBlacklistRepository;

public class TokenBlacklistUtil {
    public static void addTokenToBlacklist(String token, String reason, TokenBlacklistRepository tokenBlacklistRepository) {
        TokenBlacklist tokenBlacklist = tokenBlacklistRepository.findByToken(token);
        if (tokenBlacklist == null) {
            TokenBlacklist newTokenBlackList = TokenBlacklist.builder()
                    .token(token)
                    .reason(reason)
                    .tokenType(CommonConstant.BEARER_TOKEN)
                    .build();
            tokenBlacklistRepository.save(newTokenBlackList);
        }
    }

    public static boolean isTokenBlacklisted(String token, TokenBlacklistRepository tokenBlacklistRepository) {
        TokenBlacklist tokenBlacklist = tokenBlacklistRepository.findByToken(token);
        return tokenBlacklist != null;
    }

    public static String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0];
        }
        return ip;
    }
}
