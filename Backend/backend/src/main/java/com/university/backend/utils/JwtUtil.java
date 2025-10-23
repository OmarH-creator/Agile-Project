package com.university.backend.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;

public class JwtUtil {
    // NEVER expose secrets like this in a real app!
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor("mySuperSecretKeymySuperSecretKey".getBytes()); // use at least 256 bits

    public static String generateToken(String userEmail, Map<String, Object> claims) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(userEmail)
                .addClaims(claims)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + 1000 * 60 * 60 * 10)) // Expires in 10 hours
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }
}
