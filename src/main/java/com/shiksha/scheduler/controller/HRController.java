package com.shiksha.scheduler.controller;

import com.shiksha.scheduler.dto.JobDTO;
import com.shiksha.scheduler.dto.ScheduleRequestDTO;
import com.shiksha.scheduler.exception.ConflictException;
import com.shiksha.scheduler.model.*;
import com.shiksha.scheduler.repository.JobApplicationRepository;
import com.shiksha.scheduler.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/hr")
public class HRController {

    @Autowired private JobService jobService;
    @Autowired private InterviewService interviewService;
    @Autowired private UserService userService;
    @Autowired private JobApplicationRepository applicationRepository;
    @Autowired private PdfExportService pdfExportService;
    @Autowired private AuditLogService auditLogService;
    @Autowired private NotificationService notificationService;

    // ─── Dashboard ───────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal User hr, Model model) {
        model.addAttribute("myJobs",         jobService.findByCreatedBy(hr).size());
        model.addAttribute("openJobs",        jobService.findByCreatedBy(hr).stream()
                .filter(j -> j.getStatus() == JobStatus.OPEN).count());
        model.addAttribute("myApplications",  applicationRepository.findAllApplicationsForHR(hr).size());
        model.addAttribute("scheduledCount",  interviewService.countByStatus(InterviewStatus.SCHEDULED)
                                            + interviewService.countByStatus(InterviewStatus.RESCHEDULED));
        model.addAttribute("completedCount",  interviewService.countByStatus(InterviewStatus.COMPLETED));
        model.addAttribute("todayCount",      interviewService.countTodayInterviews());
        model.addAttribute("recentApplications", applicationRepository.findAllApplicationsForHR(hr)
                .stream().limit(5).toList());
        model.addAttribute("notifications",   notificationService.getUnread(hr));
        model.addAttribute("notifCount",      notificationService.countUnread(hr));
        model.addAttribute("hr", hr);
        return "hr/dashboard";
    }

    // ─── Jobs ────────────────────────────────────────────────────────────────────

    @GetMapping("/jobs")
    public String jobs(@AuthenticationPrincipal User hr,
                       @RequestParam(required = false) String search,
                       @RequestParam(required = false) String statusFilter,
                       Model model) {
        List<Job> jobs = jobService.findAll();
        
        if (search != null && !search.isEmpty()) {
            String q = search.toLowerCase();
            jobs = jobs.stream()
                    .filter(j -> j.getTitle().toLowerCase().contains(q) || 
                                 j.getDepartment().toLowerCase().contains(q))
                    .collect(Collectors.toList());
        }
        
        if (statusFilter != null && !statusFilter.isEmpty()) {
            jobs = jobs.stream()
                    .filter(j -> j.getStatus().name().equals(statusFilter))
                    .collect(Collectors.toList());
        }

        model.addAttribute("jobs",           jobs);
        model.addAttribute("jobDTO",         new JobDTO());
        model.addAttribute("statuses",       JobStatus.values());
        model.addAttribute("hr",             hr);
        model.addAttribute("search",         search);
        model.addAttribute("statusFilter",   statusFilter);
        addNotifModel(model, hr);
        return "hr/jobs";
    }

    @PostMapping("/jobs/create")
    public String createJob(@Valid @ModelAttribute("jobDTO") JobDTO dto,
                            BindingResult result,
                            @AuthenticationPrincipal User hr,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("jobs",    jobService.findByCreatedBy(hr));
            model.addAttribute("statuses", JobStatus.values());
            return "hr/jobs";
        }
        jobService.createJob(dto, hr);
        auditLogService.log("JOB_CREATED", hr.getEmail(), "Job", null, "Title: " + dto.getTitle());
        redirectAttributes.addFlashAttribute("successMsg", "Job post created successfully.");
        return "redirect:/hr/jobs";
    }

    @GetMapping("/jobs/{id}/edit")
    public String editJobForm(@PathVariable Long id, @AuthenticationPrincipal User hr, Model model) {
        Job job = jobService.findById(id)
                .orElseThrow(() -> new com.shiksha.scheduler.exception.ResourceNotFoundException("Job", id));
        JobDTO dto = new JobDTO();
        dto.setTitle(job.getTitle()); dto.setDescription(job.getDescription());
        dto.setDepartment(job.getDepartment()); dto.setRequiredSkills(job.getRequiredSkills());
        dto.setExperienceRequired(job.getExperienceRequired()); dto.setStatus(job.getStatus());
        model.addAttribute("jobDTO", dto); model.addAttribute("jobId", id);
        model.addAttribute("statuses", JobStatus.values());
        model.addAttribute("hr", hr);
        addNotifModel(model, hr);
        return "hr/edit-job";
    }

    @PostMapping("/jobs/{id}/edit")
    public String updateJob(@PathVariable Long id, @Valid @ModelAttribute("jobDTO") JobDTO dto,
                            BindingResult result, @AuthenticationPrincipal User hr,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return "hr/edit-job";
        jobService.updateJob(id, dto);
        auditLogService.log("JOB_UPDATED", hr.getEmail(), "Job", id, "Title: " + dto.getTitle());
        redirectAttributes.addFlashAttribute("successMsg", "Job updated successfully.");
        return "redirect:/hr/jobs";
    }

    @PostMapping("/jobs/{id}/delete")
    public String deleteJob(@PathVariable Long id, @AuthenticationPrincipal User hr,
                            RedirectAttributes redirectAttributes) {
        jobService.deleteJob(id);
        auditLogService.log("JOB_DELETED", hr.getEmail(), "Job", id, "");
        redirectAttributes.addFlashAttribute("successMsg", "Job deleted.");
        return "redirect:/hr/jobs";
    }

    // ─── Candidates / Applications ───────────────────────────────────────────────

    @GetMapping("/candidates")
    public String candidates(@AuthenticationPrincipal User hr,
                             @RequestParam(required = false) String search,
                             @RequestParam(required = false) String statusFilter,
                             Model model) {
        model.addAttribute("hr", hr);
        var apps = applicationRepository.findAllApplicationsForHR(hr);

        // Apply filters
        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase();
            apps = apps.stream()
                    .filter(a -> a.getCandidate().getFullName().toLowerCase().contains(q)
                              || a.getCandidate().getEmail().toLowerCase().contains(q))
                    .collect(Collectors.toList());
        }
        if (statusFilter != null && !statusFilter.isBlank()) {
            try {
                ApplicationStatus st = ApplicationStatus.valueOf(statusFilter);
                apps = apps.stream().filter(a -> a.getStatus() == st).collect(Collectors.toList());
            } catch (IllegalArgumentException ignored) {}
        }

        model.addAttribute("applications",    apps);
        model.addAttribute("interviewers",    userService.findByRole(Role.INTERVIEWER));
        model.addAttribute("applicationStatuses", ApplicationStatus.values());
        model.addAttribute("search",          search);
        model.addAttribute("statusFilter",    statusFilter);
        addNotifModel(model, hr);
        return "hr/candidates";
    }

    @PostMapping("/applications/{id}/shortlist")
    public String shortlist(@PathVariable Long id, @AuthenticationPrincipal User hr,
                            RedirectAttributes redirectAttributes) {
        applicationRepository.findById(id).ifPresent(app -> {
            app.setStatus(ApplicationStatus.SHORTLISTED);
            applicationRepository.save(app);
            auditLogService.log("CANDIDATE_SHORTLISTED", hr.getEmail(), "Application", id,
                    app.getCandidate().getFullName());
        });
        redirectAttributes.addFlashAttribute("successMsg", "Candidate shortlisted.");
        return "redirect:/hr/candidates";
    }

    @PostMapping("/applications/{id}/reject")
    public String reject(@PathVariable Long id, @AuthenticationPrincipal User hr,
                         RedirectAttributes redirectAttributes) {
        applicationRepository.findById(id).ifPresent(app -> {
            app.setStatus(ApplicationStatus.REJECTED);
            applicationRepository.save(app);
            auditLogService.log("CANDIDATE_REJECTED", hr.getEmail(), "Application", id,
                    app.getCandidate().getFullName());
        });
        redirectAttributes.addFlashAttribute("successMsg", "Application rejected.");
        return "redirect:/hr/candidates";
    }

    // ─── Download Resume ─────────────────────────────────────────────────────────

    @GetMapping("/applications/{id}/resume")
    public ResponseEntity<byte[]> downloadResume(@PathVariable Long id,
                                                  @Autowired FileStorageService fileStorageService) {
        return applicationRepository.findById(id).map(app -> {
            try {
                String path = app.getResumePath();
                if (path == null || path.isBlank()) return ResponseEntity.notFound().<byte[]>build();
                byte[] data = fileStorageService.loadResume(path);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"resume_" + app.getCandidate().getFullName().replace(" ", "_") + ".pdf\"")
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(data);
            } catch (Exception e) {
                return ResponseEntity.notFound().<byte[]>build();
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    // ─── Schedule Interviews (with Search) ───────────────────────────────────────

    @GetMapping("/schedule")
    public String schedulePage(@AuthenticationPrincipal User hr,
                               @RequestParam(required = false) String candidateName,
                               @RequestParam(required = false) String statusFilter,
                               @RequestParam(required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                               @RequestParam(required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                               Model model) {
        model.addAttribute("hr", hr);
        model.addAttribute("scheduleRequest", new ScheduleRequestDTO());
        model.addAttribute("applications", applicationRepository.findAllApplicationsForHR(hr)
                .stream().filter(a -> a.getStatus() == ApplicationStatus.SHORTLISTED
                        || a.getStatus() == ApplicationStatus.APPLIED).toList());
        model.addAttribute("interviewers", userService.findByRole(Role.INTERVIEWER));
        model.addAttribute("interviewStatuses", InterviewStatus.values());

        // Advanced search
        InterviewStatus statusEnum = null;
        if (statusFilter != null && !statusFilter.isBlank()) {
            try { statusEnum = InterviewStatus.valueOf(statusFilter); }
            catch (IllegalArgumentException ignored) {}
        }
        List<Interview> interviews = (candidateName != null || statusEnum != null || fromDate != null || toDate != null)
                ? interviewService.searchInterviews(candidateName, statusEnum, fromDate, toDate)
                : interviewService.findAll();

        model.addAttribute("interviews",    interviews);
        model.addAttribute("candidateName", candidateName);
        model.addAttribute("statusFilter",  statusFilter);
        model.addAttribute("fromDate",      fromDate);
        model.addAttribute("toDate",        toDate);
        addNotifModel(model, hr);
        return "hr/schedule";
    }

    @PostMapping("/schedule")
    public String scheduleInterview(@ModelAttribute("scheduleRequest") ScheduleRequestDTO dto,
                                    @AuthenticationPrincipal User hr,
                                    RedirectAttributes redirectAttributes) {
        try {
            interviewService.scheduleInterview(dto, hr);
            redirectAttributes.addFlashAttribute("successMsg", "Interview scheduled successfully.");
        } catch (ConflictException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Error: " + e.getMessage());
        }
        return "redirect:/hr/schedule";
    }

    @PostMapping("/interviews/{id}/cancel")
    public String cancelInterview(@PathVariable Long id, @AuthenticationPrincipal User hr,
                                  RedirectAttributes redirectAttributes) {
        try {
            interviewService.cancelInterview(id, hr);
            redirectAttributes.addFlashAttribute("successMsg", "Interview cancelled and slot released.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/hr/schedule";
    }

    @GetMapping("/interviews/{id}/reschedule")
    public String reschedulePage(@PathVariable Long id, @AuthenticationPrincipal User hr, Model model) {
        model.addAttribute("interviewId",    id);
        model.addAttribute("scheduleRequest", new ScheduleRequestDTO());
        model.addAttribute("interviewers",   userService.findByRole(Role.INTERVIEWER));
        addNotifModel(model, hr);
        return "hr/reschedule";
    }

    @PostMapping("/interviews/{id}/reschedule")
    public String rescheduleInterview(@PathVariable Long id,
                                      @ModelAttribute ScheduleRequestDTO dto,
                                      @AuthenticationPrincipal User hr,
                                      RedirectAttributes redirectAttributes) {
        try {
            interviewService.rescheduleInterview(id, dto, hr);
            redirectAttributes.addFlashAttribute("successMsg", "Interview rescheduled successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/hr/schedule";
    }

    // ─── Analytics Dashboard ─────────────────────────────────────────────────────

    @GetMapping("/analytics")
    public String analytics(@AuthenticationPrincipal User hr, Model model) {
        Map<String, Long> statusCounts = interviewService.getStatusCounts();
        Map<String, Long> perDay      = interviewService.getInterviewsPerDay(7);

        long totalInterviews = statusCounts.values().stream().mapToLong(Long::longValue).sum();
        long completed       = statusCounts.getOrDefault("COMPLETED", 0L);
        long successRate     = totalInterviews > 0 ? (completed * 100 / totalInterviews) : 0;

        // Hiring pipeline counts
        long applied    = applicationRepository.findAllApplicationsForHR(hr).stream()
                .filter(a -> a.getStatus() == ApplicationStatus.APPLIED).count();
        long shortlisted = applicationRepository.findAllApplicationsForHR(hr).stream()
                .filter(a -> a.getStatus() == ApplicationStatus.SHORTLISTED).count();
        long selected    = applicationRepository.findAllApplicationsForHR(hr).stream()
                .filter(a -> a.getStatus() == ApplicationStatus.SELECTED).count();
        long rejected    = applicationRepository.findAllApplicationsForHR(hr).stream()
                .filter(a -> a.getStatus() == ApplicationStatus.REJECTED).count();

        model.addAttribute("statusCounts",   statusCounts);
        model.addAttribute("perDay",         perDay);
        model.addAttribute("totalInterviews", totalInterviews);
        model.addAttribute("successRate",    successRate);
        model.addAttribute("applied",        applied);
        model.addAttribute("shortlisted",    shortlisted);
        model.addAttribute("selected",       selected);
        model.addAttribute("rejected",       rejected);
        model.addAttribute("hr", hr);
        addNotifModel(model, hr);
        return "hr/analytics";
    }

    // ─── Calendar View ───────────────────────────────────────────────────────────

    @GetMapping("/calendar")
    public String calendarPage(@AuthenticationPrincipal User hr, Model model) {
        addNotifModel(model, hr);
        model.addAttribute("hr", hr);
        return "hr/calendar";
    }

    /** REST endpoint for FullCalendar event feed */
    @GetMapping("/calendar/events")
    @ResponseBody
    public List<Map<String, Object>> calendarEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        return interviewService.findByDateRange(start, end).stream().map(i -> {
            String color = switch (i.getStatus()) {
                case SCHEDULED   -> "#3b82f6";   // blue
                case COMPLETED   -> "#22c55e";   // green
                case CANCELLED   -> "#ef4444";   // red
                case RESCHEDULED -> "#f97316";   // orange
                default          -> "#6b7280";
            };
            return Map.<String, Object>of(
                    "id",    i.getId(),
                    "title", i.getCandidate().getFullName() + " — " + i.getJob().getTitle(),
                    "start", i.getAvailability().getAvailableDate().toString()
                            + "T" + i.getAvailability().getStartTime().toString(),
                    "end",   i.getAvailability().getAvailableDate().toString()
                            + "T" + i.getAvailability().getEndTime().toString(),
                    "color", color,
                    "extendedProps", Map.of(
                            "status",      i.getStatus().name(),
                            "interviewer", i.getInterviewer().getFullName(),
                            "job",         i.getJob().getTitle()
                    )
            );
        }).collect(Collectors.toList());
    }

    // ─── PDF Export ──────────────────────────────────────────────────────────────

    @GetMapping("/export/interviews")
    public ResponseEntity<byte[]> exportPdf() {
        try {
            List<Interview> all = interviewService.findAll();
            byte[] pdf = pdfExportService.generateInterviewReport(all);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"interview_report.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─── Audit Logs (HR view of own actions) ────────────────────────────────────

    @GetMapping("/audit-logs")
    public String auditLogs(@AuthenticationPrincipal User hr, Model model) {
        model.addAttribute("logs", auditLogService.getLogsByUser(hr.getEmail()));
        model.addAttribute("hr",   hr);
        addNotifModel(model, hr);
        return "hr/audit-logs";
    }

    // ─── Helper ──────────────────────────────────────────────────────────────────

    private void addNotifModel(Model model, User user) {
        model.addAttribute("notifications", notificationService.getUnread(user));
        model.addAttribute("notifCount",    notificationService.countUnread(user));
        model.addAttribute("hr",            user);
    }
}
