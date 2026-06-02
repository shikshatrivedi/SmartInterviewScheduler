package com.shiksha.scheduler.service;

import com.shiksha.scheduler.dto.AvailabilityDTO;
import com.shiksha.scheduler.exception.ConflictException;
import com.shiksha.scheduler.exception.ResourceNotFoundException;
import com.shiksha.scheduler.exception.UnauthorizedException;
import com.shiksha.scheduler.model.Availability;
import com.shiksha.scheduler.model.User;
import com.shiksha.scheduler.repository.AvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AvailabilityServiceImpl implements AvailabilityService {

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Override
    public Availability addSlot(AvailabilityDTO dto, User interviewer) {
        if (dto.getStartTime().isAfter(dto.getEndTime()) ||
                dto.getStartTime().equals(dto.getEndTime())) {
            throw new ConflictException("Start time must be before end time.");
        }

        // Prevent overlapping slots for same interviewer
        List<Availability> existing = availabilityRepository.findByInterviewer(interviewer);
        boolean overlap = existing.stream().anyMatch(slot ->
                slot.overlapsWith(dto.getAvailableDate(), dto.getStartTime(), dto.getEndTime()));
        if (overlap) {
            throw new ConflictException("This time slot overlaps with an existing availability slot.");
        }

        Availability availability = Availability.builder()
                .interviewer(interviewer)
                .availableDate(dto.getAvailableDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .booked(false)
                .build();
        return availabilityRepository.save(availability);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Availability> findByInterviewer(User interviewer) {
        return availabilityRepository.findByInterviewer(interviewer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Availability> findUpcomingFreeSlots(User interviewer) {
        return availabilityRepository
                .findByInterviewerAndAvailableDateGreaterThanEqualOrderByAvailableDateAscStartTimeAsc(
                        interviewer, LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Availability> findFittingSlots(User interviewer, LocalDate date,
                                               LocalTime start, LocalTime end) {
        return availabilityRepository.findFittingSlots(interviewer, date, start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Availability> findNextAvailableSlots(User interviewer, LocalDate fromDate) {
        return availabilityRepository.findNextAvailableSlots(interviewer, fromDate);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Availability> findById(Long id) {
        return availabilityRepository.findById(id);
    }

    @Override
    public void deleteSlot(Long id, User interviewer) {
        Availability slot = availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Availability slot", id));
        if (!slot.getInterviewer().getId().equals(interviewer.getId())) {
            throw new UnauthorizedException("You can only delete your own slots.");
        }
        if (slot.isBooked()) {
            throw new ConflictException("Cannot delete a booked slot. Cancel the interview first.");
        }
        availabilityRepository.deleteById(id);
    }
}
