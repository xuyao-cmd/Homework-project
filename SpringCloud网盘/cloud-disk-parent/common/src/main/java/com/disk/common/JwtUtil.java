package com.disk.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
public class JwtUtil {

    private static final String SECRET = "cloud-disk-secret-key-2024-32bytes!!";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private static final long EXPIRATION = 86400000L;

    /**
     * 生成 JWT token
     */
    public static String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + EXPIRATION);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role != null ? role : "user")
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成 JWT token（兼容旧版无 role 调用）
     */
    public static String generateToken(Long userId, String username) {
        return generateToken(userId, username, "user");
    }

    private static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            log.error("解析 token 失败: {}", e.getMessage());
            return null;
        }
    }

    public static String getUsernameFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return (String) claims.get("username");
        } catch (Exception e) {
            log.error("解析 token 失败: {}", e.getMessage());
            return null;
        }
    }

    public static String getRoleFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return (String) claims.get("role");
        } catch (Exception e) {
            log.error("解析 token 失败: {}", e.getMessage());
            return null;
        }
    }

    public static boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            log.error("验证 token 失败: {}", e.getMessage());
            return false;
        }
    }
}
