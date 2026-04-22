package com.chris.robot_server.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${token.expire.seconds}")
    private Integer expireSeconds;

    @Value("${token.jwtSecret}")
    private String jwtSecret;

    /**
     * 生成JWT Token
     */
    // public String generateToken(Long userId) {
    // return Jwts.builder()
    // .setSubject(userId.toString())
    // .setIssuedAt(new Date())
    // .setExpiration(new Date(System.currentTimeMillis() + expireSeconds * 1000))
    // .signWith(SignatureAlgorithm.HS256, jwtSecret)
    // .compact();
    // }

    /**
     * 生成普通 API JWT Token（包含 EMQX 需要的 uid claim）
     */
    public String generateToken(Long userId) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("uid", userId.toString()) // 🔴 EMQX 常用
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expireSeconds * 1000L))
                .signWith(
                        Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)),
                        SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 🔴 专为 EMQX 生成 Token（5天过期）
     */
    public String generateEmqxToken(Long userId) {
        long now = System.currentTimeMillis();
        long expSeconds = now / 1000 + 3600L * 24 * 5; // 5天

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("uid", userId.toString()) // EMQX 需要
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(expSeconds * 1000))
                .signWith(
                        Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)),
                        SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析JWT Token
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(
                Keys.hmacShaKeyFor(
                    jwtSecret.getBytes(StandardCharsets.UTF_8)
                )
            )
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    public Long getUserId(String token) {
        try {
            Claims claims = parseToken(token);
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            System.err.println("getUserId 异常: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return null; // 或返回 null
        }
    }

    /**
     * 一次性验证 Token 并返回 userId
     * - token 无效（格式错、签名错、已过期） → 返回 null
     * - token 有效 → 返回 userId
     */
    public Long getValidUserId(String token) {
        Claims claims = safeParse(token); // 只解析一次
        if (claims == null) {
            return null;
        }
        if (claims.getExpiration().before(new Date())) {
            return null;
        }
        try {
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            System.err.println("getUserId 异常: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取 uid（EMQX 专用）
     */
    public Long getUid(String token) {
        try {
            Claims claims = parseToken(token);
            Object uid = claims.get("uid");
            return uid != null ? Long.valueOf(uid.toString()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断 Token 是否完全有效：格式合法 + 签名正确 + 未过期
     */
    public boolean isValid(String token) {
        Claims claims = safeParse(token); // 使用已有的 safeParse，避免重复 try-catch
        if (claims == null) {
            return false;
        }
        return !claims.getExpiration().before(new Date());
    }

    /**
     * 判断 Token 是否已过期
     * - 如果 token 格式/签名错误 → 返回 true（视为已过期）
     * - 如果能解析成功 → 返回真实过期状态
     */
    public boolean isExpired(String token) {
        Claims claims = safeParse(token);
        if (claims == null) {
            return true; // 无效token视为已过期
        }
        return claims.getExpiration().before(new Date());
    }

    /**
     * 尝试解析 token，失败时返回 null
     */
    public Claims safeParse(String token) {
        try {
            return parseToken(token);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 刷新JWT Token（前提是旧token未过期）
     */
    public String refreshToken(String token) {
        Claims claims = parseToken(token);
        // if (!isValid(token)) {
        // throw new RuntimeException("Token已过期，无法刷新");
        // }

        return Jwts.builder()
                .setSubject(claims.getSubject())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expireSeconds * 1000))
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    /**
     * 刷新 Token（保留所有 claims，包括 uid）
     */
    public String refreshToken2(String token) {
        Claims claims = parseToken(token);

        return Jwts.builder()
                .setSubject(claims.getSubject())
                .setClaims(claims) // 保留原有 claims（含 uid）
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expireSeconds * 1000L))
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }
}
