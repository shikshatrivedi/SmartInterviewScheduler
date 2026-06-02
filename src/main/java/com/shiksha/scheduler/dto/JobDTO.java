package com.shiksha.scheduler.dto;

import com.shiksha.scheduler.model.JobStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class JobDTO {

    @NotBlank(message = "Job title is required")
    private String title;

    private String description;

    @NotBlank(message = "Department is required")
    private String department;

    private String requiredSkills;
    private String experienceRequired;

    @NotNull(message = "Job status is required")
    private JobStatus status;

    public JobDTO() {}

    public String    getTitle()                    { return title; }
    public void      setTitle(String v)            { this.title = v; }
    public String    getDescription()              { return description; }
    public void      setDescription(String v)      { this.description = v; }
    public String    getDepartment()               { return department; }
    public void      setDepartment(String v)       { this.department = v; }
    public String    getRequiredSkills()           { return requiredSkills; }
    public void      setRequiredSkills(String v)   { this.requiredSkills = v; }
    public String    getExperienceRequired()       { return experienceRequired; }
    public void      setExperienceRequired(String v){ this.experienceRequired = v; }
    public JobStatus getStatus()                   { return status; }
    public void      setStatus(JobStatus v)        { this.status = v; }
}
