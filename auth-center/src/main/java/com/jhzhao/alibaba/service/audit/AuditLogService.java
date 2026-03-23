package com.jhzhao.alibaba.service.audit;

import com.jhzhao.alibaba.entity.audit.AuditLog;
import com.jhzhao.alibaba.repository.audit.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 审计日志服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * 异步记录审计日志
     */
    @Async("auditLogExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAsync(AuditLog auditLog) {
        try {
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }

    /**
     * 记录审计日志 (同步)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditLog auditLog) {
        try {
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }

    /**
     * 记录登录事件
     */
    public void logLogin(String username, String clientId, boolean success, String errorMessage) {
        AuditLog auditLog = createBaseAuditLog();
        auditLog.setOperationType(AuditLog.OperationType.LOGIN.name());
        auditLog.setUsername(username);
        auditLog.setClientId(clientId);
        auditLog.setResponseStatus(success ? 200 : 401);
        auditLog.setErrorMessage(errorMessage);

        logAsync(auditLog);
    }

    /**
     * 记录登出事件
     */
    public void logLogout(String username, String clientId) {
        AuditLog auditLog = createBaseAuditLog();
        auditLog.setOperationType(AuditLog.OperationType.LOGOUT.name());
        auditLog.setUsername(username);
        auditLog.setClientId(clientId);
        auditLog.setResponseStatus(200);

        logAsync(auditLog);
    }

    /**
     * 记录 Token 发放事件
     */
    public void logTokenIssue(String username, String clientId, String grantType, boolean success, String errorMessage) {
        AuditLog auditLog = createBaseAuditLog();
        auditLog.setOperationType(AuditLog.OperationType.TOKEN_ISSUE.name());
        auditLog.setUsername(username);
        auditLog.setClientId(clientId);
        auditLog.setRequestParams("grant_type=" + grantType);
        auditLog.setResponseStatus(success ? 200 : 400);
        auditLog.setErrorMessage(errorMessage);

        logAsync(auditLog);
    }

    /**
     * 记录 Token 刷新事件
     */
    public void logTokenRefresh(String username, String clientId, boolean success, String errorMessage) {
        AuditLog auditLog = createBaseAuditLog();
        auditLog.setOperationType(AuditLog.OperationType.TOKEN_REFRESH.name());
        auditLog.setUsername(username);
        auditLog.setClientId(clientId);
        auditLog.setResponseStatus(success ? 200 : 400);
        auditLog.setErrorMessage(errorMessage);

        logAsync(auditLog);
    }

    /**
     * 记录 Token 撤销事件
     */
    public void logTokenRevoke(String username, String clientId, String tokenType, boolean success) {
        AuditLog auditLog = createBaseAuditLog();
        auditLog.setOperationType(AuditLog.OperationType.TOKEN_REVOKE.name());
        auditLog.setUsername(username);
        auditLog.setClientId(clientId);
        auditLog.setRequestParams("token_type=" + tokenType);
        auditLog.setResponseStatus(success ? 200 : 400);

        logAsync(auditLog);
    }

    /**
     * 记录授权码发放事件
     */
    public void logAuthorizationCode(String username, String clientId, boolean success) {
        AuditLog auditLog = createBaseAuditLog();
        auditLog.setOperationType(AuditLog.OperationType.AUTHORIZATION_CODE.name());
        auditLog.setUsername(username);
        auditLog.setClientId(clientId);
        auditLog.setResponseStatus(success ? 200 : 400);

        logAsync(auditLog);
    }

    /**
     * 记录客户端凭证事件
     */
    public void logClientCredentials(String clientId, boolean success, String errorMessage) {
        AuditLog auditLog = createBaseAuditLog();
        auditLog.setOperationType(AuditLog.OperationType.CLIENT_CREDENTIALS.name());
        auditLog.setClientId(clientId);
        auditLog.setResponseStatus(success ? 200 : 401);
        auditLog.setErrorMessage(errorMessage);

        logAsync(auditLog);
    }

    /**
     * 创建基础审计日志对象
     */
    private AuditLog createBaseAuditLog() {
        AuditLog auditLog = new AuditLog();

        // 获取当前请求信息
        Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(attributes -> attributes instanceof ServletRequestAttributes)
                .map(attributes -> ((ServletRequestAttributes) attributes).getRequest())
                .ifPresent(request -> {
                    auditLog.setIpAddress(getClientIp(request));
                    auditLog.setUserAgent(request.getHeader("User-Agent"));
                    auditLog.setRequestUri(request.getRequestURI());
                    auditLog.setRequestMethod(request.getMethod());
                });

        return auditLog;
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理情况，取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 统计指定时间内的登录失败次数
     */
    public long countFailedLogins(String username, LocalDateTime since) {
        Long count = auditLogRepository.countFailedLoginsSince(username, since);
        return count != null ? count : 0;
    }
}
