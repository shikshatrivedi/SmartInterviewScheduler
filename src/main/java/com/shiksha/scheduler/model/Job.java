package com.shiksha.scheduler.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String department;

    @Column(name = "required_skills", columnDefinition = "TEXT")
    private String requiredSkills;

    @Column(name = "experience_required")
    private String experienceRequired;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobApplication> applications = new ArrayList<>();

    // ─── Constructors ────────────────────────────────────────────────────────────
    public Job() {}

    // ─── Builder ─────────────────────────────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String title;
        private String description;
        private String department;
        private String requiredSkills;
        private String experienceRequired;
        private JobStatus status = JobStatus.OPEN;
        private User createdBy;

        public Builder title(String v)              { this.title = v; return this; }
        public Builder description(String v)        { this.description = v; return this; }
        public Builder department(String v)         { this.department = v; return this; }
        public Builder requiredSkills(String v)     { this.requiredSkills = v; return this; }
        public Builder experienceRequired(String v) { this.experienceRequired = v; return this; }
        public Builder status(JobStatus v)          { this.status = v; return this; }
        public Builder createdBy(User v)            { this.createdBy = v; return this; }

        public Job build() {
            Job j = new Job();
            j.title              = this.title;
            j.description        = this.description;
            j.department         = this.department;
            j.requiredSkills     = this.requiredSkills;
            j.experienceRequired = this.experienceRequired;
            j.status             = this.status;
            j.createdBy          = this.createdBy;
            return j;
        }
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────────
    public Long        getId()                     { return id; }
    public String      getTitle()                  { return title; }
    public void        setTitle(String v)          { this.title = v; }
    public String      getDescription()            { return description; }
    public void        setDescription(String v)    { this.description = v; }
    public String      getDepartment()             { return department; }
    public void        setDepartment(String v)     { this.department = v; }
    public String      getRequiredSkills()         { return requiredSkills; }
    public void        setRequiredSkills(String v) { this.requiredSkills = v; }
    public String      getExperienceRequired()     { return experienceRequired; }
    public void        setExperienceRequired(String v){ this.experienceRequired = v; }
    public JobStatus   getStatus()                 { return status; }
    public void        setStatus(JobStatus v)      { this.status = v; }
    public User        getCreatedBy()              { return createdBy; }
    public void        setCreatedBy(User v)        { this.createdBy = v; }
    public LocalDateTime getCreatedAt()            { return createdAt; }
    public List<JobApplication> getApplications()  { return applications; }
}
