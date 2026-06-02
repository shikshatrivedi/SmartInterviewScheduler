package com.shiksha.scheduler.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "interview_feedback")
public class InterviewFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interviewer_id", nullable = false)
    private User interviewer;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(name = "technical_score")
    private Integer technicalScore;

    @Column(name = "communication_score")
    private Integer communicationScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedbackResult result;

    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    // ─── Constructors ─────────────────────────────────────────────────────────────
    public InterviewFeedback() {}

    // ─── Builder ──────────────────────────────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Interview interview;
        private User interviewer;
        private String comments;
        private Integer technicalScore;
        private Integer communicationScore;
        private FeedbackResult result;

        public Builder interview(Interview v)         { this.interview = v; return this; }
        public Builder interviewer(User v)            { this.interviewer = v; return this; }
        public Builder comments(String v)             { this.comments = v; return this; }
        public Builder technicalScore(Integer v)      { this.technicalScore = v; return this; }
        public Builder communicationScore(Integer v)  { this.communicationScore = v; return this; }
        public Builder result(FeedbackResult v)       { this.result = v; return this; }

        public InterviewFeedback build() {
            InterviewFeedback f = new InterviewFeedback();
            f.interview          = this.interview;
            f.interviewer        = this.interviewer;
            f.comments           = this.comments;
            f.technicalScore     = this.technicalScore;
            f.communicationScore = this.communicationScore;
            f.result             = this.result;
            return f;
        }
    }

    // ─── Getters ──────────────────────────────────────────────────────────────────
    public Long          getId()                    { return id; }
    public Interview     getInterview()             { return interview; }
    public void          setInterview(Interview v)  { this.interview = v; }
    public User          getInterviewer()           { return interviewer; }
    public String        getComments()              { return comments; }
    public Integer       getTechnicalScore()        { return technicalScore; }
    public Integer       getCommunicationScore()    { return communicationScore; }
    public FeedbackResult getResult()               { return result; }
    public LocalDateTime getSubmittedAt()           { return submittedAt; }
}
