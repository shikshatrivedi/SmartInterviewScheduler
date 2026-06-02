package com.shiksha.scheduler.service;

import com.shiksha.scheduler.dto.FeedbackDTO;
import com.shiksha.scheduler.dto.ScheduleRequestDTO;
import com.shiksha.scheduler.exception.ConflictException;
import com.shiksha.scheduler.exception.ResourceNotFoundException;
import com.shiksha.scheduler.exception.UnauthorizedException;
import com.shiksha.scheduler.model.*;
import com.shiksha.scheduler.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


/**
 * ══════════════════════════════════════════════════════════════════
 *  SMART SCHEDULING ENGINE  —  Core Business Logic (Enhanced)
 * ══════════════════════════════════════════════════════════════════
 *
 * Algorithm overview (Upgraded Greedy):
 *
 *  scheduleInterview(request):
 *   1. Resolve candidate, interviewer from application → job.
 *   2. If HR provided preferred date/time  → EXACT-SLOT MODE:
 *        Picks the first non-conflicting slot on that date/time.
 *   3. If no date/time given → SMART AUTO-ASSIGN MODE (upgraded):
 *        Picks the interviewer's slot by sorting candidates by:
 *        (a) Least active interviews (workload balance)
 *        (b) Earliest available date (time priority)
 *   4. Marks slot booked, saves Interview, updates ApplicationStatus.
 *   5. Fires: EmailNotification, AuditLog entry, in-app Notification.
 * ══════════════════════════════════════════════════════════════════
 */
@Service
@Transactional
public class InterviewServiceImpl implements InterviewService {

    private static final Logger log = LoggerFactory.getLogger(InterviewServiceImpl.class);

    private static final int MIN_SLOT_DURATION_MINUTES = 30;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    @Autowired private InterviewRepository    interviewRepository;
    @Autowired private AvailabilityRepository availabilityRepository;
    @Autowired private JobApplicationRepository applicationRepository;
    @Autowired private InterviewFeedbackRepository feedbackRepository;
    @Autowired private UserRepository          userRepository;
    @Autowired private AuditLogService         auditLogService;
    @Autowired private NotificationService     notificationService;
    @Autowired private EmailService            emailService;

    // ────────────────────────────────────────────────────────────────────────────
    //  SCHEDULE
    // ────────────────────────────────────────────────────────────────────────────

    @Override
    public Interview scheduleInterview(ScheduleRequestDTO dto, User scheduledBy) {
        log.info("Scheduling interview for application ID: {} by user: {}", dto.getApplicationId(), scheduledBy.getEmail());
        
        JobApplication application = applicationRepository.findById(dto.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application", dto.getApplicationId()));

        User candidate   = application.getCandidate();
        Job  job         = application.getJob();

        User interviewer = userRepository.findById(dto.getInterviewerId())
                .orElseThrow(() -> new ResourceNotFoundException("Interviewer", dto.getInterviewerId()));

        if (interviewer.getRole() != Role.INTERVIEWER) {
            throw new UnauthorizedException("Selected user is not an interviewer.");
        }

        Availability chosenSlot = resolveSlot(dto, candidate, interviewer);
        chosenSlot.setBooked(true);
        availabilityRepository.save(chosenSlot);

        Interview interview = Interview.builder()
                .job(job)
                .candidate(candidate)
                .interviewer(interviewer)
                .availability(chosenSlot)
                .status(InterviewStatus.SCHEDULED)
                .notes(dto.getNotes())
                .build();
        Interview saved = interviewRepository.save(interview);

        application.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
        applicationRepository.save(application);

        // — Side effects —
        String date = chosenSlot.getAvailableDate().format(DATE_FMT);
        String time = chosenSlot.getStartTime().format(TIME_FMT);

        emailService.sendInterviewScheduled(candidate.getEmail(), candidate.getFullName(),
                job.getTitle(), interviewer.getFullName(), date, time);
        emailService.sendInterviewScheduled(interviewer.getEmail(), interviewer.getFullName(),
                job.getTitle(), candidate.getFullName() + " (candidate)", date, time);

        notificationService.send(candidate,
                "Your interview for <b>" + job.getTitle() + "</b> is scheduled on " + date + " at " + time,
                "/candidate/dashboard");
        notificationService.send(interviewer,
                "New interview assigned: <b>" + candidate.getFullName() + "</b> for " + job.getTitle() + " on " + date,
                "/interviewer/dashboard");

        auditLogService.log("INTERVIEW_SCHEDULED", scheduledBy.getEmail(),
                "Interview", saved.getId(),
                "For: " + candidate.getFullName() + " | Job: " + job.getTitle()
                + " | Date: " + date + " | Time: " + time);

        log.info("Successfully scheduled interview ID: {} for candidate: {}", saved.getId(), candidate.getEmail());
        return saved;
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  RESCHEDULE
    // ────────────────────────────────────────────────────────────────────────────

    @Override
    public Interview rescheduleInterview(Long interviewId, ScheduleRequestDTO dto, User scheduledBy) {
        log.info("Rescheduling interview ID: {} by user: {}", interviewId, scheduledBy.getEmail());
        
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview", interviewId));

        if (interview.getStatus() == InterviewStatus.CANCELLED) {
            throw new ConflictException("Cannot reschedule a cancelled interview.");
        }
        if (interview.getStatus() == InterviewStatus.COMPLETED) {
            throw new ConflictException("Cannot reschedule a completed interview.");
        }

        Availability oldSlot = interview.getAvailability();
        oldSlot.setBooked(false);
        availabilityRepository.save(oldSlot);

        User interviewer = userRepository.findById(dto.getInterviewerId())
                .orElseThrow(() -> new ResourceNotFoundException("Interviewer", dto.getInterviewerId()));

        dto.setApplicationId(
                applicationRepository.findByCandidateAndJob(interview.getCandidate(), interview.getJob())
                        .map(a -> a.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Application not found")));

        Availability newSlot = resolveSlot(dto, interview.getCandidate(), interviewer);
        newSlot.setBooked(true);
        availabilityRepository.save(newSlot);

        interview.setInterviewer(interviewer);
        interview.setAvailability(newSlot);
        interview.setStatus(InterviewStatus.RESCHEDULED);
        interview.setNotes(dto.getNotes());
        Interview saved = interviewRepository.save(interview);

        // — Side effects —
        String date = newSlot.getAvailableDate().format(DATE_FMT);
        String time = newSlot.getStartTime().format(TIME_FMT);

        emailService.sendInterviewRescheduled(interview.getCandidate().getEmail(),
                interview.getCandidate().getFullName(), interview.getJob().getTitle(), date, time);
        emailService.sendInterviewRescheduled(interviewer.getEmail(),
                "Interviewer", interview.getJob().getTitle(), date, time);

        notificationService.send(interview.getCandidate(),
                "Your interview for <b>" + interview.getJob().getTitle() + "</b> has been rescheduled to " + date,
                "/candidate/dashboard");

        auditLogService.log("INTERVIEW_RESCHEDULED", scheduledBy.getEmail(),
                "Interview", interviewId,
                "New date: " + date + " | Interviewer: " + interviewer.getFullName());

        log.info("Successfully rescheduled interview ID: {}. New interviewer: {}", saved.getId(), interviewer.getEmail());
        return saved;
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  CANCEL
    // ────────────────────────────────────────────────────────────────────────────

    @Override
    public void cancelInterview(Long interviewId, User cancelledBy) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview", interviewId));

        if (interview.getStatus() == InterviewStatus.CANCELLED) {
            throw new ConflictException("Interview is already cancelled.");
        }

        interview.getAvailability().setBooked(false);
        availabilityRepository.save(interview.getAvailability());

        interview.setStatus(InterviewStatus.CANCELLED);
        interviewRepository.save(interview);

        applicationRepository.findByCandidateAndJob(interview.getCandidate(), interview.getJob())
                .ifPresent(app -> {
                    app.setStatus(ApplicationStatus.SHORTLISTED);
                    applicationRepository.save(app);
                });

        // — Side effects —
        emailService.sendInterviewCancelled(interview.getCandidate().getEmail(),
                interview.getCandidate().getFullName(), interview.getJob().getTitle());

        notificationService.send(interview.getCandidate(),
                "Your interview for <b>" + interview.getJob().getTitle() + "</b> has been cancelled.",
                "/candidate/dashboard");

        auditLogService.log("INTERVIEW_CANCELLED", cancelledBy.getEmail(),
                "Interview", interviewId,
                "Candidate: " + interview.getCandidate().getFullName()
                + " | Job: " + interview.getJob().getTitle());

        log.warn("Interview ID: {} was cancelled by: {}", interviewId, cancelledBy.getEmail());
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  FEEDBACK
    // ────────────────────────────────────────────────────────────────────────────

    @Override
    public Interview submitFeedback(FeedbackDTO dto, User interviewer) {
        Interview interview = interviewRepository.findById(dto.getInterviewId())
                .orElseThrow(() -> new ResourceNotFoundException("Interview", dto.getInterviewId()));

        if (!interview.getInterviewer().getId().equals(interviewer.getId())) {
            throw new UnauthorizedException("You are not the assigned interviewer for this interview.");
        }
        if (feedbackRepository.findByInterview_Id(interview.getId()).isPresent()) {
            throw new ConflictException("Feedback already submitted for this interview.");
        }

        InterviewFeedback feedback = InterviewFeedback.builder()
                .interview(interview)
                .interviewer(interviewer)
                .comments(dto.getComments())
                .technicalScore(dto.getTechnicalScore())
                .communicationScore(dto.getCommunicationScore())
                .result(dto.getResult())
                .build();
        feedbackRepository.save(feedback);

        interview.setStatus(InterviewStatus.COMPLETED);
        interview.setFeedback(feedback);

        applicationRepository.findByCandidateAndJob(interview.getCandidate(), interview.getJob())
                .ifPresent(app -> {
                    app.setStatus(dto.getResult() == FeedbackResult.PASS
                            ? ApplicationStatus.SELECTED
                            : dto.getResult() == FeedbackResult.FAIL
                                    ? ApplicationStatus.REJECTED
                                    : ApplicationStatus.SHORTLISTED);
                    applicationRepository.save(app);
                });

        String result = dto.getResult().name();
        notificationService.send(interview.getCandidate(),
                "Feedback submitted for your <b>" + interview.getJob().getTitle()
                + "</b> interview. Result: " + result,
                "/candidate/dashboard");

        auditLogService.log("FEEDBACK_SUBMITTED", interviewer.getEmail(),
                "Interview", interview.getId(),
                "Result: " + result
                + " | Tech score: " + dto.getTechnicalScore()
                + " | Comm score: " + dto.getCommunicationScore());

        return interviewRepository.save(interview);
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  CORE ENGINE: resolveSlot  (UPGRADED — Least Busy Interviewer Logic)
    // ────────────────────────────────────────────────────────────────────────────

    private Availability resolveSlot(ScheduleRequestDTO dto, User candidate, User interviewer) {
        List<Availability> candidates;

        if (dto.getPreferredDate() != null &&
                dto.getPreferredStartTime() != null &&
                dto.getPreferredEndTime() != null) {

            // ── MODE A: Exact slot request ────────────────────────────────────
            candidates = availabilityRepository.findFittingSlots(
                    interviewer,
                    dto.getPreferredDate(),
                    dto.getPreferredStartTime(),
                    dto.getPreferredEndTime());

            if (candidates.isEmpty()) {
                throw new ConflictException(
                        "No available slot found for interviewer on " + dto.getPreferredDate()
                        + " between " + dto.getPreferredStartTime() + " and " + dto.getPreferredEndTime()
                        + ". Please choose a different time or interviewer.");
            }

        } else {
            // ── MODE B: Smart Auto-Assign (upgraded) ──────────────────────────
            // Fetch all future free slots for the selected interviewer
            candidates = availabilityRepository.findNextAvailableSlots(interviewer, LocalDate.now());

            // Filter by minimum duration
            candidates = candidates.stream()
                    .filter(slot -> java.time.Duration.between(
                            slot.getStartTime(), slot.getEndTime()).toMinutes()
                            >= MIN_SLOT_DURATION_MINUTES)
                    .toList();

            if (candidates.isEmpty()) {
                throw new ConflictException(
                        "Interviewer has no free upcoming availability slots. "
                        + "Please ask the interviewer to add availability first.");
            }

            // UPGRADE: Sort by interviewer workload (least busy first), then by earliest date
            long workload = interviewRepository.countActiveInterviewsForInterviewer(interviewer);

            // If the assigned interviewer has high workload (>3 active), find least busy across all
            if (workload > 3) {
                List<User> allInterviewers = userRepository.findAll().stream()
                        .filter(u -> u.getRole() == Role.INTERVIEWER)
                        .sorted(Comparator.comparingLong(u ->
                                interviewRepository.countActiveInterviewsForInterviewer(u)))
                        .toList();

                for (User altInterviewer : allInterviewers) {
                    if (altInterviewer.getId().equals(interviewer.getId())) continue;
                    List<Availability> altSlots = availabilityRepository
                            .findNextAvailableSlots(altInterviewer, LocalDate.now())
                            .stream()
                            .filter(s -> java.time.Duration.between(
                                    s.getStartTime(), s.getEndTime()).toMinutes()
                                    >= MIN_SLOT_DURATION_MINUTES)
                            .toList();
                    if (!altSlots.isEmpty()) {
                        // Check if less busy
                        long altWorkload = interviewRepository.countActiveInterviewsForInterviewer(altInterviewer);
                        if (altWorkload < workload) {
                            candidates = altSlots;
                            break;
                        }
                    }
                }
            }
        }

        // Greedy: pick the first non-conflicting slot
        for (Availability slot : candidates) {
            boolean conflict = interviewRepository.hasConflict(
                    candidate,
                    slot.getAvailableDate(),
                    slot.getStartTime(),
                    slot.getEndTime());
            if (!conflict) {
                return slot;
            }
        }

        throw new ConflictException(
                "All available slots conflict with the candidate's existing interview schedule. "
                + "Please try a different date/time or interviewer.");
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  QUERIES
    // ────────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Optional<Interview> findById(Long id) {
        return interviewRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Interview> findByCandidate(User candidate) {
        return interviewRepository.findByCandidate(candidate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Interview> findByInterviewer(User interviewer) {
        return interviewRepository
                .findByInterviewerOrderByAvailability_AvailableDateAscAvailability_StartTimeAsc(interviewer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Interview> findAll() {
        return interviewRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(InterviewStatus status) {
        return interviewRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTodayInterviews() {
        return interviewRepository.countTodayInterviews(LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Interview> searchInterviews(String candidateName, InterviewStatus status,
                                            LocalDate fromDate, LocalDate toDate) {
        return interviewRepository.searchInterviews(
                (candidateName != null && !candidateName.isBlank()) ? candidateName : null,
                status,
                fromDate,
                toDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Interview> findByDateRange(LocalDate start, LocalDate end) {
        return interviewRepository.findByDateRange(start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getStatusCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (InterviewStatus s : InterviewStatus.values()) {
            counts.put(s.name(), interviewRepository.countByStatus(s));
        }
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getInterviewsPerDay(int days) {
        Map<String, Long> result = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM");
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            long count = interviewRepository.findByDateRange(d, d).size();
            result.put(d.format(fmt), count);
        }
        return result;
    }
}
