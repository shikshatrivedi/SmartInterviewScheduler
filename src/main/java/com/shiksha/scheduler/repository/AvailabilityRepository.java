package com.shiksha.scheduler.repository;

import com.shiksha.scheduler.model.Availability;
import com.shiksha.scheduler.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    List<Availability> findByInterviewer(User interviewer);

    List<Availability> findByInterviewerAndBooked(User interviewer, boolean booked);

    List<Availability> findByInterviewerAndAvailableDateGreaterThanEqualOrderByAvailableDateAscStartTimeAsc(
            User interviewer, LocalDate date);

    /**
     * Finds free slots for an interviewer that contain the requested [start, end] window on a given date.
     * Used by the scheduling engine to find candidate slots.
     */
    @Query("""
            SELECT a FROM Availability a
            WHERE a.interviewer = :interviewer
              AND a.availableDate = :date
              AND a.startTime <= :requestedStart
              AND a.endTime   >= :requestedEnd
              AND a.booked = false
            ORDER BY a.startTime ASC
            """)
    List<Availability> findFittingSlots(@Param("interviewer") User interviewer,
                                        @Param("date") LocalDate date,
                                        @Param("requestedStart") LocalTime requestedStart,
                                        @Param("requestedEnd") LocalTime requestedEnd);

    /**
     * Finds ALL free slots for an interviewer from today onwards (greedy auto-assign mode).
     */
    @Query("""
            SELECT a FROM Availability a
            WHERE a.interviewer = :interviewer
              AND a.availableDate >= :fromDate
              AND a.booked = false
            ORDER BY a.availableDate ASC, a.startTime ASC
            """)
    List<Availability> findNextAvailableSlots(@Param("interviewer") User interviewer,
                                              @Param("fromDate") LocalDate fromDate);
}
