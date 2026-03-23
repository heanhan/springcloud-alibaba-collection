package com.jhzhao.alibaba.entity.audit;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审计日志实体类
 */
@Entity
@Table(name = "sys_audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 操作类型: LOGIN, LOGOUT, TOKEN_ISSUE, TOKEN_REVOKE, TOKEN_REFRESH
     */
    @Column(name = "operation_type", nullable = false, length = 50)
    private String operationType;

    @Column(length = 50)
    private String username;

    @Column(name = "client_id", length = 100)
    private String clientId;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "request_uri", length = 500)
    private String requestUri;

    @Column(name = "request_method", length = 10)
    private String requestMethod;

    @Column(name = "request_params", columnDefinition = "TEXT")
    private String requestParams;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "execution_time")
    private Long executionTime;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 操作类型枚举
     */
    public enum OperationType {
        LOGIN("用户登录"),
        LOGOUT("用户登出"),
        TOKEN_ISSUE("Token发放"),
        TOKEN_REFRESH("Token刷新"),
        TOKEN_REVOKE("Token撤销"),
        AUTHORIZATION_CODE("授权码发放"),
        CLIENT_CREDENTIALS("客户端凭证");

        private final String description;

        OperationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
