package com.shiksha.scheduler.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public class AvailabilityDTO {

    @NotNull(message = "Interviewer ID is required")
    private Long interviewerId;

    @NotNull(message = "Date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate availableDate;

    @NotNull(message = "Start time is required")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime endTime;

    public AvailabilityDTO() {}

    public Long      getInterviewerId()               { return interviewerId; }
    public void      setInterviewerId(Long v)         { this.interviewerId = v; }
    public LocalDate getAvailableDate()               { return availableDate; }
    public void      setAvailableDate(LocalDate v)    { this.availableDate = v; }
    public LocalTime getStartTime()                   { return startTime; }
    public void      setStartTime(LocalTime v)        { this.startTime = v; }
    public LocalTime getEndTime()                     { return endTime; }
    public void      setEndTime(LocalTime v)          { this.endTime = v; }
}
