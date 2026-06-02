package com.shiksha.scheduler.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "availability")
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interviewer_id", nullable = false)
    private User interviewer;

    @Column(name = "available_date", nullable = false)
    private LocalDate availableDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "is_booked", nullable = false)
    private boolean booked = false;

    // ─── Constructors ─────────────────────────────────────────────────────────────
    public Availability() {}

    // ─── Builder ──────────────────────────────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private User interviewer;
        private LocalDate availableDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private boolean booked = false;

        public Builder interviewer(User v)      { this.interviewer = v; return this; }
        public Builder availableDate(LocalDate v){ this.availableDate = v; return this; }
        public Builder startTime(LocalTime v)   { this.startTime = v; return this; }
        public Builder endTime(LocalTime v)     { this.endTime = v; return this; }
        public Builder booked(boolean v)        { this.booked = v; return this; }

        public Availability build() {
            Availability a = new Availability();
            a.interviewer   = this.interviewer;
            a.availableDate = this.availableDate;
            a.startTime     = this.startTime;
            a.endTime       = this.endTime;
            a.booked        = this.booked;
            return a;
        }
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────────
    public Long      getId()                   { return id; }
    public User      getInterviewer()          { return interviewer; }
    public void      setInterviewer(User v)    { this.interviewer = v; }
    public LocalDate getAvailableDate()        { return availableDate; }
    public void      setAvailableDate(LocalDate v){ this.availableDate = v; }
    public LocalTime getStartTime()            { return startTime; }
    public void      setStartTime(LocalTime v) { this.startTime = v; }
    public LocalTime getEndTime()              { return endTime; }
    public void      setEndTime(LocalTime v)   { this.endTime = v; }
    public boolean   isBooked()                { return booked; }
    public void      setBooked(boolean v)      { this.booked = v; }

    // ─── Domain Logic ─────────────────────────────────────────────────────────────
    public boolean overlapsWith(LocalDate date, LocalTime start, LocalTime end) {
        if (!this.availableDate.equals(date)) return false;
        return this.startTime.isBefore(end) && this.endTime.isAfter(start);
    }
}
