package com.daily.dailychineseculture.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 提供JWT token的生成、解析和验证功能
 * 
 * @author Java后端架构师
 * @since 2026-02-25
 */
@Component
public class JwtUtils {
    
    // JWT密钥（生产环境应从配置文件读取）
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    
    // token过期时间（7天）
    private static final long EXPIRATION_TIME = 604800000;
    
    /**
     * 生成 JWT token（简化版，仅包含 userId 和 username）
     * 
     * @param userId 用户 ID
     * @param username 用户名
     * @return JWT token 字符串
     */
    public String generateToken(Long userId, String username) {
        return generateToken(userId, username, null, null);
    }
    
    /**
     * 生成 JWT token（支持多角色）
     * 
     * @param userId 用户 ID
     * @param username 用户名
     * @param currentRole 当前角色
     * @param campId 营期 ID（可选，管理员为 null）
     * @return JWT token 字符串
     */
    public String generateToken(Long userId, String username, String currentRole, Integer campId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);
            
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("currentRole", currentRole);
        if (campId != null) {
            claims.put("campId", campId);
        }
            
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SECRET_KEY)
                .compact();
    }
    
    /**
     * 从token中解析用户ID
     * 
     * @param token JWT token
     * @return 用户ID
     * @throws RuntimeException 当token无效或过期时抛出
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            throw new RuntimeException("Token解析失败: " + e.getMessage());
        }
    }
    
    /**
     * 从 token 中解析当前角色
     * 
     * @param token JWT token
     * @return 当前角色
     */
    public String getCurrentRoleFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("currentRole", String.class);
        } catch (Exception e) {
            throw new RuntimeException("Token 解析失败：" + e.getMessage());
        }
    }
        
    /**
     * 从 token 中解析营期 ID
     * 
     * @param token JWT token
     * @return 营期 ID，如果没有则返回 null
     */
    public Integer getCampIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("campId", Integer.class);
        } catch (Exception e) {
            return null;
        }
    }
        
    /**
     * 从 token 中解析用户名
     * 
     * @param token JWT token
     * @return 用户名
     * @throws RuntimeException 当token无效或过期时抛出
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            throw new RuntimeException("Token解析失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证token是否有效
     * 
     * @param token JWT token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 解析token获取Claims
     * 
     * @param token JWT token
     * @return Claims对象
     */
    private Claims parseToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 检查token是否过期
     * 
     * @param token JWT token
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}