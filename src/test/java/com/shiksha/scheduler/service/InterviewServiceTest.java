package com.shiksha.scheduler.service;

import com.shiksha.scheduler.dto.ScheduleRequestDTO;
import com.shiksha.scheduler.exception.ConflictException;
import com.shiksha.scheduler.model.*;
import com.shiksha.scheduler.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InterviewServiceTest {

    @Mock private InterviewRepository interviewRepository;
    @Mock private AvailabilityRepository availabilityRepository;
    @Mock private JobApplicationRepository applicationRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditLogService auditLogService;
    @Mock private NotificationService notificationService;
    @Mock private EmailService emailService;

    @InjectMocks
    private InterviewServiceImpl interviewService;

    private User hr;
    private User candidate;
    private User interviewer;
    private Job job;
    private JobApplication application;
    private Availability availability;

    @BeforeEach
    void setUp() {
        hr = User.builder().email("hr@test.com").role(Role.HR).build();
        ReflectionTestUtils.setField(hr, "id", 1L);

        candidate = User.builder().fullName("John Doe").email("john@test.com").role(Role.CANDIDATE).build();
        ReflectionTestUtils.setField(candidate, "id", 2L);

        interviewer = User.builder().fullName("Jane Smith").email("jane@test.com").role(Role.INTERVIEWER).build();
        ReflectionTestUtils.setField(interviewer, "id", 3L);

        job = Job.builder().title("Java Developer").build();
        ReflectionTestUtils.setField(job, "id", 1L);

        application = JobApplication.builder().candidate(candidate).job(job).status(ApplicationStatus.SHORTLISTED).build();
        ReflectionTestUtils.setField(application, "id", 1L);

        availability = Availability.builder()
                .interviewer(interviewer)
                .availableDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .booked(false)
                .build();
        ReflectionTestUtils.setField(availability, "id", 1L);
    }

    @Test
    void scheduleInterview_Success() {
        // Arrange
        ScheduleRequestDTO dto = new ScheduleRequestDTO();
        dto.setApplicationId(1L);
        dto.setInterviewerId(3L);
        dto.setPreferredDate(availability.getAvailableDate());
        dto.setPreferredStartTime(availability.getStartTime());
        dto.setPreferredEndTime(availability.getEndTime());

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(userRepository.findById(3L)).thenReturn(Optional.of(interviewer));
        when(availabilityRepository.findFittingSlots(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(availability));
        when(interviewRepository.hasConflict(any(), any(), any(), any())).thenReturn(false);
        when(interviewRepository.save(any(Interview.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Interview result = interviewService.scheduleInterview(dto, hr);

        // Assert
        assertNotNull(result);
        assertEquals(InterviewStatus.SCHEDULED, result.getStatus());
        assertTrue(availability.isBooked());
        verify(emailService, times(2)).sendInterviewScheduled(any(), any(), any(), any(), any(), any());
    }

    @Test
    void scheduleInterview_Conflict_ThrowsException() {
        // Arrange
        ScheduleRequestDTO dto = new ScheduleRequestDTO();
        dto.setApplicationId(1L);
        dto.setInterviewerId(3L);
        dto.setPreferredDate(availability.getAvailableDate());
        dto.setPreferredStartTime(availability.getStartTime());
        dto.setPreferredEndTime(availability.getEndTime());

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(userRepository.findById(3L)).thenReturn(Optional.of(interviewer));
        when(availabilityRepository.findFittingSlots(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(availability));
        when(interviewRepository.hasConflict(any(), any(), any(), any())).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> interviewService.scheduleInterview(dto, hr));
    }
}
