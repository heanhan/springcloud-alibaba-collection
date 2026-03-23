package com.jhzhao.alibaba.entity.oauth2;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Token 黑名单实体类
 * 用于存储已撤销的 Token
 */
@Entity
@Table(name = "sys_token_blacklist")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenBlacklist implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Token JTI (JWT ID)
     */
    @Column(name = "token_id", unique = true, nullable = false, length = 100)
    private String tokenId;

    /**
     * Token 类型: access_token, refresh_token
     */
    @Column(name = "token_type", nullable = false, length = 20)
    private String tokenType;

    @Column(length = 50)
    private String username;

    @Column(name = "client_id", length = 100)
    private String clientId;

    /**
     * Token 过期时间
     */
    @Column(name = "expiration_time", nullable = false)
    private LocalDateTime expirationTime;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public TokenBlacklist(String tokenId, String tokenType, String username, String clientId, LocalDateTime expirationTime) {
        this.tokenId = tokenId;
        this.tokenType = tokenType;
        this.username = username;
        this.clientId = clientId;
        this.expirationTime = expirationTime;
    }
}
