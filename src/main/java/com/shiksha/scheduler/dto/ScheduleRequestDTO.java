package com.shiksha.scheduler.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO used by HR to request interview scheduling.
 * If date/time are provided, the engine tries to fit into a slot matching that window.
 * If date/time are omitted, the engine auto-assigns the earliest available slot (greedy).
 */
public class ScheduleRequestDTO {

    @NotNull(message = "Application ID is required")
    private Long applicationId;

    @NotNull(message = "Interviewer ID is required")
    private Long interviewerId;

    // Optional: if provided → exact-slot mode; if not → greedy auto-assign mode
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate preferredDate;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime preferredStartTime;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime preferredEndTime;

    private String notes;

    public ScheduleRequestDTO() {}

    public Long      getApplicationId()                { return applicationId; }
    public void      setApplicationId(Long v)          { this.applicationId = v; }
    public Long      getInterviewerId()                { return interviewerId; }
    public void      setInterviewerId(Long v)          { this.interviewerId = v; }
    public LocalDate getPreferredDate()                { return preferredDate; }
    public void      setPreferredDate(LocalDate v)     { this.preferredDate = v; }
    public LocalTime getPreferredStartTime()           { return preferredStartTime; }
    public void      setPreferredStartTime(LocalTime v){ this.preferredStartTime = v; }
    public LocalTime getPreferredEndTime()             { return preferredEndTime; }
    public void      setPreferredEndTime(LocalTime v)  { this.preferredEndTime = v; }
    public String    getNotes()                        { return notes; }
    public void      setNotes(String v)                { this.notes = v; }
}
