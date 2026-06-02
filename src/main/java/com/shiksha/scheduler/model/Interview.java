package com.shiksha.scheduler.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "interviews")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interviewer_id", nullable = false)
    private User interviewer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "availability_id", nullable = false)
    private Availability availability;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewStatus status = InterviewStatus.SCHEDULED;

    @Column(name = "scheduled_at", updatable = false)
    private LocalDateTime scheduledAt = LocalDateTime.now();

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
    private InterviewFeedback feedback;

    @PreUpdate
    protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    // ─── Constructors ─────────────────────────────────────────────────────────────
    public Interview() {}

    // ─── Builder ──────────────────────────────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Job job;
        private User candidate;
        private User interviewer;
        private Availability availability;
        private InterviewStatus status = InterviewStatus.SCHEDULED;
        private String notes;

        public Builder job(Job v)                   { this.job = v; return this; }
        public Builder candidate(User v)            { this.candidate = v; return this; }
        public Builder interviewer(User v)          { this.interviewer = v; return this; }
        public Builder availability(Availability v) { this.availability = v; return this; }
        public Builder status(InterviewStatus v)    { this.status = v; return this; }
        public Builder notes(String v)              { this.notes = v; return this; }

        public Interview build() {
            Interview i = new Interview();
            i.job          = this.job;
            i.candidate    = this.candidate;
            i.interviewer  = this.interviewer;
            i.availability = this.availability;
            i.status       = this.status;
            i.notes        = this.notes;
            return i;
        }
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────────
    public Long            getId()                     { return id; }
    public Job             getJob()                    { return job; }
    public void            setJob(Job v)               { this.job = v; }
    public User            getCandidate()              { return candidate; }
    public void            setCandidate(User v)        { this.candidate = v; }
    public User            getInterviewer()            { return interviewer; }
    public void            setInterviewer(User v)      { this.interviewer = v; }
    public Availability    getAvailability()           { return availability; }
    public void            setAvailability(Availability v){ this.availability = v; }
    public InterviewStatus getStatus()                 { return status; }
    public void            setStatus(InterviewStatus v){ this.status = v; }
    public LocalDateTime   getScheduledAt()            { return scheduledAt; }
    public String          getNotes()                  { return notes; }
    public void            setNotes(String v)          { this.notes = v; }
    public LocalDateTime   getUpdatedAt()              { return updatedAt; }
    public InterviewFeedback getFeedback()             { return feedback; }
    public void            setFeedback(InterviewFeedback v){ this.feedback = v; }
}
