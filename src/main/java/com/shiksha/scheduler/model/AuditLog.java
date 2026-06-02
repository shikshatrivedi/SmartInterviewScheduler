package com.shiksha.scheduler.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action;           // e.g. "INTERVIEW_SCHEDULED"

    @Column(name = "performed_by", nullable = false)
    private String performedBy;      // user email

    @Column(name = "target_type")
    private String targetType;       // "Interview", "Job", etc.

    @Column(name = "target_id")
    private Long targetId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public AuditLog() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String action;
        private String performedBy;
        private String targetType;
        private Long targetId;
        private String details;

        public Builder action(String v)      { this.action = v; return this; }
        public Builder performedBy(String v) { this.performedBy = v; return this; }
        public Builder targetType(String v)  { this.targetType = v; return this; }
        public Builder targetId(Long v)      { this.targetId = v; return this; }
        public Builder details(String v)     { this.details = v; return this; }

        public AuditLog build() {
            AuditLog log = new AuditLog();
            log.action      = this.action;
            log.performedBy = this.performedBy;
            log.targetType  = this.targetType;
            log.targetId    = this.targetId;
            log.details     = this.details;
            return log;
        }
    }

    public Long          getId()          { return id; }
    public String        getAction()      { return action; }
    public String        getPerformedBy() { return performedBy; }
    public String        getTargetType()  { return targetType; }
    public Long          getTargetId()    { return targetId; }
    public String        getDetails()     { return details; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
}
