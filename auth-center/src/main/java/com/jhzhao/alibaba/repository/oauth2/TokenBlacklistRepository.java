package com.jhzhao.alibaba.repository.oauth2;

import com.jhzhao.alibaba.entity.oauth2.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Token 黑名单 Repository
 */
@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    /**
     * 根据 Token ID 查询
     */
    Optional<TokenBlacklist> findByTokenId(String tokenId);

    /**
     * 检查 Token 是否在黑名单中
     */
    boolean existsByTokenId(String tokenId);

    /**
     * 删除过期的 Token 记录
     */
    @Modifying
    @Query("DELETE FROM TokenBlacklist t WHERE t.expirationTime < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * 根据用户名删除所有 Token 记录
     */
    @Modifying
    void deleteByUsername(String username);

    /**
     * 根据客户端ID删除所有 Token 记录
     */
    @Modifying
    void deleteByClientId(String clientId);
}
