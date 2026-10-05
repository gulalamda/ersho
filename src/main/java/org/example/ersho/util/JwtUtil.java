package org.example.ersho.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtil {


    private static final String SE_KEY = "5oiR5pyJ54K56K6o5Y6M5L2g5LqG5Y6M54Om5L2g5LqG";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SE_KEY.getBytes(StandardCharsets.UTF_8));
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
