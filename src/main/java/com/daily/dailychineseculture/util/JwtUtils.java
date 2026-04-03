package com.daily.dailychineseculture.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
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
    
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    // 固化密钥字符串（必须足够长，至少256位用于HS256算法）
    // 生产环境建议从配置文件读取，格式：@Value("${jwt.secret}")
    private static final String SECRET_KEY_STRING = "DailyChineseCultureSecretKey1234567890abcdefghijklmnopqrstuv";
    
    // 基于固定字符串生成 SecretKey 对象
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8));
    
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
     * @param currentRole 当前角色（COURSE_ADMIN, ARCHIVE_ADMIN, SUPER_ADMIN）
     * @param campId 营期 ID（可选，普通管理员有值，SUPER_ADMIN 为 null）
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
     * 生成 JWT token（支持自定义 Claims）
     * 
     * @param claims 自定义 Claims
     * @return JWT token 字符串
     */
    public String generateToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);
        
        // 确保包含必要的 claims
        if (!claims.containsKey("userId")) {
            throw new IllegalArgumentException("Claims 必须包含 userId");
        }
        
        String username = (String) claims.getOrDefault("username", "user");
        
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
            logger.error("解析用户ID失败: {}", e.getMessage());
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
     * @param token JWT token（可带或不带 Bearer 前缀）
     * @return Claims对象
     * @throws RuntimeException 当token无效、过期或签名不匹配时抛出
     */
    private Claims parseToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new RuntimeException("Token为空");
        }
            
        // 规范化：剥离 "Bearer " 前缀（不区分大小写）
        String pureToken = token;
        if (token.toLowerCase().startsWith("bearer ")) {
            pureToken = token.substring(7);
        }
            
        // 校验纯净Token是否有效
        if (!StringUtils.hasText(pureToken)) {
            throw new RuntimeException("Token格式无效");
        }
    
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(pureToken)
                    .getBody();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            logger.warn("Token已过期: {}", e.getMessage());
            throw new RuntimeException("Token已过期，请重新登录");
        } catch (io.jsonwebtoken.security.SignatureException e) {
            logger.error("Token签名验证失败: {}", e.getMessage());
            throw new RuntimeException("Token签名无效");
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            logger.error("Token格式错误: {}", e.getMessage());
            throw new RuntimeException("Token格式错误");
        } catch (Exception e) {
            logger.error("Token解析异常: {}", e.getMessage());
            throw new RuntimeException("Token解析失败: " + e.getMessage());
        }
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