package com.jhzhao.alibaba.repository.audit;

import com.jhzhao.alibaba.entity.audit.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志 Repository
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * 根据操作类型查询日志
     */
    List<AuditLog> findByOperationType(String operationType);

    /**
     * 根据用户名查询日志
     */
    List<AuditLog> findByUsernameOrderByCreatedAtDesc(String username);

    /**
     * 根据用户名查询日志（分页）
     */
    Page<AuditLog> findByUsernameOrderByCreatedAtDesc(String username, Pageable pageable);

    /**
     * 根据客户端ID查询日志
     */
    List<AuditLog> findByClientIdOrderByCreatedAtDesc(String clientId);

    /**
     * 查询时间范围内的日志
     */
    List<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);

    /**
     * 根据操作类型和时间范围查询日志
     */
    @Query("SELECT a FROM AuditLog a WHERE a.operationType = :operationType AND a.createdAt BETWEEN :start AND :end ORDER BY a.createdAt DESC")
    List<AuditLog> findByOperationTypeAndTimeRange(@Param("operationType") String operationType,
                                                    @Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end);

    /**
     * 统计指定时间内的登录失败次数
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.username = :username AND a.operationType = 'LOGIN' AND a.responseStatus != 200 AND a.createdAt > :since")
    Long countFailedLoginsSince(@Param("username") String username, @Param("since") LocalDateTime since);
}
