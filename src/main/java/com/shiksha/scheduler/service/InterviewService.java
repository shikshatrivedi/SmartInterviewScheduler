package com.shiksha.scheduler.service;

import com.shiksha.scheduler.dto.FeedbackDTO;
import com.shiksha.scheduler.dto.ScheduleRequestDTO;
import com.shiksha.scheduler.model.Interview;
import com.shiksha.scheduler.model.User;

import java.util.List;
import java.util.Optional;

public interface InterviewService {

    /**
     * Core smart scheduling method. Supports both exact-slot and greedy auto-assign modes.
     */
    Interview scheduleInterview(ScheduleRequestDTO dto, User scheduledBy);

    /**
     * Reschedule an existing interview: releases old slot, re-runs engine.
     */
    Interview rescheduleInterview(Long interviewId, ScheduleRequestDTO dto, User scheduledBy);

    /**
     * Cancel an interview and release the booked availability slot.
     */
    void cancelInterview(Long interviewId, User cancelledBy);

    /**
     * Submit interviewer feedback and mark interview as COMPLETED.
     */
    Interview submitFeedback(FeedbackDTO dto, User interviewer);

    Optional<Interview> findById(Long id);
    List<Interview> findByCandidate(User candidate);
    List<Interview> findByInterviewer(User interviewer);
    List<Interview> findAll();
    long countByStatus(com.shiksha.scheduler.model.InterviewStatus status);
    long countTodayInterviews();

    // Advanced search
    List<Interview> searchInterviews(String candidateName,
                                     com.shiksha.scheduler.model.InterviewStatus status,
                                     java.time.LocalDate fromDate,
                                     java.time.LocalDate toDate);

    // Calendar
    List<Interview> findByDateRange(java.time.LocalDate start, java.time.LocalDate end);

    // Analytics
    java.util.Map<String, Long> getStatusCounts();
    java.util.Map<String, Long> getInterviewsPerDay(int days);
}
