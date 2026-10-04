package org.example.ersho.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Map;



public class JwtUtil {
    private static final String SE_KEY="5oiR5pyJ54K56K6o5Y6M5L2g5LqG5Y6M54Om5L2g5LqG";
    private static final SecretKey KEY =
            Keys.hmacShaKeyFor(SE_KEY.getBytes(StandardCharsets.UTF_8));
    private static final long EX_TIME=24*60*60*1000;

    public static String getToken(Map<String,Object> claims){
        long now = System.currentTimeMillis();
            String token =Jwts.builder()
                    .signWith(SignatureAlgorithm.HS256,SE_KEY)
                .addClaims(claims)
                .setExpiration(new Date(now+EX_TIME))
                .compact();
        return token;
    }
    public static Claims parseToken(String token){
        return Jwts.parser().verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }



    }

