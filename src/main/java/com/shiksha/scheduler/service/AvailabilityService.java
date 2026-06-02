package com.shiksha.scheduler.service;

import com.shiksha.scheduler.dto.AvailabilityDTO;
import com.shiksha.scheduler.model.Availability;
import com.shiksha.scheduler.model.User;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AvailabilityService {
    Availability addSlot(AvailabilityDTO dto, User interviewer);
    List<Availability> findByInterviewer(User interviewer);
    List<Availability> findUpcomingFreeSlots(User interviewer);
    List<Availability> findFittingSlots(User interviewer, LocalDate date, LocalTime start, LocalTime end);
    List<Availability> findNextAvailableSlots(User interviewer, LocalDate fromDate);
    Optional<Availability> findById(Long id);
    void deleteSlot(Long id, User interviewer);
}
