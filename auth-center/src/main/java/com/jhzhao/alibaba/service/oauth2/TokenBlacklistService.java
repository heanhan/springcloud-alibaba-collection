package com.jhzhao.alibaba.service.oauth2;

import com.jhzhao.alibaba.entity.oauth2.TokenBlacklist;
import com.jhzhao.alibaba.repository.oauth2.TokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Token 黑名单服务
 * 使用 Redis 作为一级缓存，数据库作为持久化存储
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private static final String TOKEN_BLACKLIST_PREFIX = "oauth2:token:blacklist:";

    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 将 Token 加入黑名单
     *
     * @param tokenId        Token JTI
     * @param tokenType      Token 类型 (access_token, refresh_token)
     * @param username       用户名
     * @param clientId       客户端ID
     * @param expirationTime Token 过期时间
     */
    @Transactional
    public void addToBlacklist(String tokenId, String tokenType, String username, String clientId, Date expirationTime) {
        log.info("Adding token to blacklist: tokenId={}, type={}, user={}, client={}",
                tokenId, tokenType, username, clientId);

        // 1. 保存到数据库
        LocalDateTime expireTime = expirationTime.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        TokenBlacklist blacklist = new TokenBlacklist(tokenId, tokenType, username, clientId, expireTime);
        tokenBlacklistRepository.save(blacklist);

        // 2. 保存到 Redis (设置过期时间为 Token 剩余有效期)
        String redisKey = TOKEN_BLACKLIST_PREFIX + tokenId;
        long ttlSeconds = Duration.between(LocalDateTime.now(), expireTime).getSeconds();
        if (ttlSeconds > 0) {
            redisTemplate.opsForValue().set(redisKey, tokenType, ttlSeconds, TimeUnit.SECONDS);
            log.debug("Token added to Redis blacklist with TTL: {} seconds", ttlSeconds);
        }
    }

    /**
     * 检查 Token 是否在黑名单中
     *
     * @param tokenId Token JTI
     * @return true 如果在黑名单中
     */
    public boolean isBlacklisted(String tokenId) {
        // 1. 先检查 Redis (高性能)
        String redisKey = TOKEN_BLACKLIST_PREFIX + tokenId;
        Boolean existsInRedis = redisTemplate.hasKey(redisKey);
        if (Boolean.TRUE.equals(existsInRedis)) {
            log.debug("Token found in Redis blacklist: {}", tokenId);
            return true;
        }

        // 2. 再检查数据库 (兜底)
        boolean existsInDb = tokenBlacklistRepository.existsByTokenId(tokenId);
        if (existsInDb) {
            log.debug("Token found in database blacklist: {}", tokenId);
            // 同步到 Redis (防止缓存穿透)
            TokenBlacklist blacklist = tokenBlacklistRepository.findByTokenId(tokenId).orElse(null);
            if (blacklist != null) {
                long ttlSeconds = Duration.between(LocalDateTime.now(), blacklist.getExpirationTime()).getSeconds();
                if (ttlSeconds > 0) {
                    redisTemplate.opsForValue().set(redisKey, blacklist.getTokenType(), ttlSeconds, TimeUnit.SECONDS);
                }
            }
            return true;
        }

        return false;
    }

    /**
     * 撤销用户的所有 Token
     *
     * @param username 用户名
     */
    @Transactional
    public void revokeAllUserTokens(String username) {
        log.info("Revoking all tokens for user: {}", username);
        tokenBlacklistRepository.deleteByUsername(username);
        // 清理 Redis 中相关的 key (通过 pattern 匹配)
        // 注意: 生产环境建议使用 Redis Keyspace Notifications 或定期清理
    }

    /**
     * 撤销客户端的所有 Token
     *
     * @param clientId 客户端ID
     */
    @Transactional
    public void revokeAllClientTokens(String clientId) {
        log.info("Revoking all tokens for client: {}", clientId);
        tokenBlacklistRepository.deleteByClientId(clientId);
    }

    /**
     * 定期清理过期的 Token 记录
     * 每天凌晨 2 点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Cleaning up expired token blacklist entries");
        int deleted = tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Deleted {} expired token blacklist entries", deleted);
    }
}
