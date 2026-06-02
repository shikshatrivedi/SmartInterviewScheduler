package com.shiksha.scheduler.repository;

import com.shiksha.scheduler.model.Interview;
import com.shiksha.scheduler.model.InterviewStatus;
import com.shiksha.scheduler.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByCandidate(User candidate);

    List<Interview> findByInterviewer(User interviewer);

    List<Interview> findByStatus(InterviewStatus status);

    List<Interview> findByCandidateAndStatus(User candidate, InterviewStatus status);

    List<Interview> findByInterviewerOrderByAvailability_AvailableDateAscAvailability_StartTimeAsc(
            User interviewer);

    /**
     * Check if a candidate already has an interview overlapping with an availability slot.
     * Used to detect candidate-side scheduling conflicts.
     */
    @Query("""
            SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END
            FROM Interview i
            WHERE i.candidate = :candidate
              AND i.status NOT IN ('CANCELLED')
              AND i.availability.availableDate = :date
              AND i.availability.startTime < :endTime
              AND i.availability.endTime > :startTime
            """)
    boolean hasConflict(@Param("candidate") User candidate,
                        @Param("date") java.time.LocalDate date,
                        @Param("startTime") java.time.LocalTime startTime,
                        @Param("endTime") java.time.LocalTime endTime);

    long countByStatus(InterviewStatus status);

    @Query("SELECT COUNT(i) FROM Interview i WHERE i.availability.availableDate = :today")
    long countTodayInterviews(@Param("today") LocalDate today);

    // Smart scheduling: count active interviews per interviewer to find least busy
    @Query("""
            SELECT COUNT(i) FROM Interview i
            WHERE i.interviewer = :interviewer
              AND i.status IN ('SCHEDULED', 'RESCHEDULED')
            """)
    long countActiveInterviewsForInterviewer(@Param("interviewer") User interviewer);

    // Advanced search: filter by candidate name (partial), status, and date
    @Query("""
            SELECT i FROM Interview i
            WHERE (:candidateName IS NULL OR LOWER(i.candidate.fullName) LIKE LOWER(CONCAT('%', :candidateName, '%')))
              AND (:status IS NULL OR i.status = :status)
              AND (:fromDate IS NULL OR i.availability.availableDate >= :fromDate)
              AND (:toDate IS NULL OR i.availability.availableDate <= :toDate)
            ORDER BY i.availability.availableDate DESC, i.availability.startTime ASC
            """)
    List<Interview> searchInterviews(
            @Param("candidateName") String candidateName,
            @Param("status") InterviewStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    // Calendar: all interviews between two dates
    @Query("""
            SELECT i FROM Interview i
            WHERE i.availability.availableDate BETWEEN :start AND :end
            ORDER BY i.availability.availableDate ASC, i.availability.startTime ASC
            """)
    List<Interview> findByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
