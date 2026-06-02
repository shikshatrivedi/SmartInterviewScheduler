package com.shiksha.scheduler.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_applications",
        uniqueConstraints = @UniqueConstraint(columnNames = {"candidate_id", "job_id"}))
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Column(name = "resume_path")
    private String resumePath;

    @Column(name = "applied_at", updatable = false)
    private LocalDateTime appliedAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    // ─── Constructors ─────────────────────────────────────────────────────────────
    public JobApplication() {}

    // ─── Builder ──────────────────────────────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private User candidate;
        private Job job;
        private ApplicationStatus status = ApplicationStatus.APPLIED;
        private String coverLetter;
        private String resumePath;

        public Builder candidate(User v)         { this.candidate = v; return this; }
        public Builder job(Job v)                { this.job = v; return this; }
        public Builder status(ApplicationStatus v){ this.status = v; return this; }
        public Builder coverLetter(String v)     { this.coverLetter = v; return this; }
        public Builder resumePath(String v)       { this.resumePath = v; return this; }

        public JobApplication build() {
            JobApplication a = new JobApplication();
            a.candidate   = this.candidate;
            a.job         = this.job;
            a.status      = this.status;
            a.coverLetter = this.coverLetter;
            a.resumePath  = this.resumePath;
            return a;
        }
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────────
    public Long              getId()               { return id; }
    public User              getCandidate()        { return candidate; }
    public void              setCandidate(User v)  { this.candidate = v; }
    public Job               getJob()             { return job; }
    public void              setJob(Job v)        { this.job = v; }
    public ApplicationStatus getStatus()          { return status; }
    public void              setStatus(ApplicationStatus v){ this.status = v; }
    public String            getCoverLetter()     { return coverLetter; }
    public String            getResumePath()      { return resumePath; }
    public void              setResumePath(String v){ this.resumePath = v; }
    public LocalDateTime     getAppliedAt()       { return appliedAt; }
    public LocalDateTime     getUpdatedAt()       { return updatedAt; }
}
