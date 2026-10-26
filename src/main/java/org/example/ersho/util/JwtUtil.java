package org.example.ersho.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;


import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtil {

    // 未配置时生成本次运行专用密钥；部署时设置 JWT_SECRET，避免重启后登录失效。
    private static final SecretKey KEY = signingKey();

    private static SecretKey signingKey() {
        String secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.isBlank()) {
            return Jwts.SIG.HS256.key().build();
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    private static final long EX_TIME = 24 * 60 * 60 * 1000L;
    private JwtUtil() {
    }
    public static String getToken(Map<String, Object> claims) {

        long now = System.currentTimeMillis();
        return Jwts.builder()
                .addClaims(claims)
                .issuedAt(new Date(now))
                .expiration(new Date(now + EX_TIME))
                .signWith(KEY)
                .compact();
    }
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }
}
