package com.shiksha.scheduler.service;

import com.shiksha.scheduler.model.AuditLog;
import com.shiksha.scheduler.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void log(String action, String performedBy, String targetType, Long targetId, String details) {
        AuditLog entry = AuditLog.builder()
                .action(action)
                .performedBy(performedBy)
                .targetType(targetType)
                .targetId(targetId)
                .details(details)
                .build();
        auditLogRepository.save(entry);
    }

    public List<AuditLog> getRecentLogs(int limit) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));
    }

    public List<AuditLog> getLogsByUser(String email) {
        return auditLogRepository.findByPerformedByOrderByCreatedAtDesc(email);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 1000));
    }
}
