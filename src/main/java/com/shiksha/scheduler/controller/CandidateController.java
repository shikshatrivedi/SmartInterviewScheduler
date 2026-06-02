package com.shiksha.scheduler.controller;

import com.shiksha.scheduler.exception.ConflictException;
import com.shiksha.scheduler.model.*;
import com.shiksha.scheduler.repository.JobApplicationRepository;
import com.shiksha.scheduler.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/candidate")
public class CandidateController {

    @Autowired private JobService              jobService;
    @Autowired private JobApplicationRepository applicationRepository;
    @Autowired private InterviewService        interviewService;
    @Autowired private FileStorageService      fileStorageService;
    @Autowired private NotificationService     notificationService;

    // ─── Dashboard ───────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal User candidate, Model model) {
        var myApps       = applicationRepository.findByCandidate(candidate);
        var myInterviews = interviewService.findByCandidate(candidate);

        model.addAttribute("totalApplications",  myApps.size());
        model.addAttribute("pendingApplications", myApps.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.APPLIED).count());
        model.addAttribute("scheduledInterviews", myInterviews.stream()
                .filter(i -> i.getStatus().name().equals("SCHEDULED")
                          || i.getStatus().name().equals("RESCHEDULED")).count());
        model.addAttribute("completedInterviews", myInterviews.stream()
                .filter(i -> i.getStatus().name().equals("COMPLETED")).count());
        model.addAttribute("recentApplications", myApps.stream().limit(5).toList());
        model.addAttribute("interviews",          myInterviews);
        
        addNotifModel(model, candidate);
        return "candidate/dashboard";
    }

    // ─── Browse & Apply for Jobs ─────────────────────────────────────────────────

    @GetMapping("/jobs")
    public String browseJobs(@AuthenticationPrincipal User candidate,
                             @RequestParam(required = false) String search,
                             Model model) {
        List<Job> openJobs = jobService.findByStatus(JobStatus.OPEN);
        
        if (search != null && !search.isEmpty()) {
            String q = search.toLowerCase();
            openJobs = openJobs.stream()
                    .filter(j -> j.getTitle().toLowerCase().contains(q) || 
                                 j.getDepartment().toLowerCase().contains(q))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        var appliedJobIds = applicationRepository.findByCandidate(candidate)
                .stream().map(a -> a.getJob().getId()).toList();
        
        model.addAttribute("jobs",          openJobs);
        model.addAttribute("appliedJobIds", appliedJobIds);
        model.addAttribute("search",        search);
        addNotifModel(model, candidate);
        return "candidate/jobs";
    }

    @PostMapping("/jobs/{id}/apply")
    public String applyForJob(@PathVariable("id") Long jobId,
                              @RequestParam(required = false) String coverLetter,
                              @AuthenticationPrincipal User candidate,
                              RedirectAttributes redirectAttributes) {
        try {
            var job = jobService.findById(jobId)
                    .orElseThrow(() -> new com.shiksha.scheduler.exception.ResourceNotFoundException("Job", jobId));
            if (applicationRepository.existsByCandidateAndJob(candidate, job)) {
                throw new ConflictException("You have already applied for this job.");
            }
            var application = JobApplication.builder()
                    .candidate(candidate).job(job)
                    .coverLetter(coverLetter)
                    .status(ApplicationStatus.APPLIED)
                    .build();
            applicationRepository.save(application);
            redirectAttributes.addFlashAttribute("successMsg", "Application submitted for: " + job.getTitle());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/candidate/jobs";
    }

    // ─── My Applications & Status ────────────────────────────────────────────────

    @GetMapping("/status")
    public String applicationStatus(@AuthenticationPrincipal User candidate, Model model) {
        model.addAttribute("applications", applicationRepository.findByCandidate(candidate));
        model.addAttribute("interviews",   interviewService.findByCandidate(candidate));
        addNotifModel(model, candidate);
        return "candidate/status";
    }

    // ─── Resume Upload ───────────────────────────────────────────────────────────

    @PostMapping("/resume/upload")
    public String uploadResume(@RequestParam("file") MultipartFile file,
                               @RequestParam("applicationId") Long applicationId,
                               @AuthenticationPrincipal User candidate,
                               RedirectAttributes redirectAttributes) {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMsg", "Please select a PDF file to upload.");
                return "redirect:/candidate/dashboard";
            }
            if (!file.getContentType().equals("application/pdf")) {
                redirectAttributes.addFlashAttribute("errorMsg", "Only PDF files are accepted.");
                return "redirect:/candidate/dashboard";
            }
            String filename = fileStorageService.storeResume(file, candidate.getId());
            applicationRepository.findById(applicationId).ifPresent(app -> {
                app.setResumePath(filename);
                applicationRepository.save(app);
            });
            redirectAttributes.addFlashAttribute("successMsg", "Resume uploaded successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Upload failed: " + e.getMessage());
        }
        return "redirect:/candidate/dashboard";
    }

    /** Download own resume */
    @GetMapping("/resume/download/{applicationId}")
    public ResponseEntity<byte[]> downloadResume(@PathVariable Long applicationId,
                                                  @AuthenticationPrincipal User candidate) {
        return applicationRepository.findById(applicationId).map(app -> {
            if (!app.getCandidate().getId().equals(candidate.getId()))
                return ResponseEntity.status(403).<byte[]>build();
            try {
                byte[] data = fileStorageService.loadResume(app.getResumePath());
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"my_resume.pdf\"")
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(data);
            } catch (Exception e) {
                return ResponseEntity.notFound().<byte[]>build();
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    private void addNotifModel(Model model, User user) {
        model.addAttribute("notifications", notificationService.getUnread(user));
        model.addAttribute("notifCount",    notificationService.countUnread(user));
        model.addAttribute("candidate",    user);
    }
}
