package com.shiksha.scheduler.controller;

import com.shiksha.scheduler.dto.AvailabilityDTO;
import com.shiksha.scheduler.dto.FeedbackDTO;
import com.shiksha.scheduler.model.User;
import com.shiksha.scheduler.service.AvailabilityService;
import com.shiksha.scheduler.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/interviewer")
public class InterviewerController {

    @Autowired private AvailabilityService availabilityService;
    @Autowired private InterviewService interviewService;
    @Autowired private com.shiksha.scheduler.service.NotificationService notificationService;

    // ─── Dashboard ───────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal User interviewer, Model model) {
        var interviews = interviewService.findByInterviewer(interviewer);
        var upcomingSlots = availabilityService.findUpcomingFreeSlots(interviewer);

        model.addAttribute("totalInterviews", interviews.size());
        model.addAttribute("upcomingSlots", upcomingSlots.size());
        model.addAttribute("completedInterviews", interviews.stream()
                .filter(i -> i.getStatus().name().equals("COMPLETED")).count());
        model.addAttribute("scheduledInterviews", interviews.stream()
                .filter(i -> i.getStatus().name().equals("SCHEDULED")
                          || i.getStatus().name().equals("RESCHEDULED")).count());
        model.addAttribute("recentInterviews", interviews.stream().limit(5).toList());
        
        addNotifModel(model, interviewer);
        return "interviewer/dashboard";
    }

    // ─── Availability ────────────────────────────────────────────────────────────

    @GetMapping("/availability")
    public String availabilityPage(@AuthenticationPrincipal User interviewer, Model model) {
        model.addAttribute("slots", availabilityService.findByInterviewer(interviewer));
        model.addAttribute("availabilityDTO", new AvailabilityDTO());
        model.addAttribute("interviewerId", interviewer.getId());
        addNotifModel(model, interviewer);
        return "interviewer/availability";
    }

    @PostMapping("/availability/add")
    public String addSlot(@Valid @ModelAttribute("availabilityDTO") AvailabilityDTO dto,
                          BindingResult result,
                          @AuthenticationPrincipal User interviewer,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        if (result.hasErrors()) {
            model.addAttribute("slots", availabilityService.findByInterviewer(interviewer));
            model.addAttribute("interviewerId", interviewer.getId());
            addNotifModel(model, interviewer);
            return "interviewer/availability";
        }
        dto.setInterviewerId(interviewer.getId());
        try {
            availabilityService.addSlot(dto, interviewer);
            redirectAttributes.addFlashAttribute("successMsg", "Availability slot added successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/interviewer/availability";
    }

    @PostMapping("/availability/{id}/delete")
    public String deleteSlot(@PathVariable Long id,
                             @AuthenticationPrincipal User interviewer,
                             RedirectAttributes redirectAttributes) {
        try {
            availabilityService.deleteSlot(id, interviewer);
            redirectAttributes.addFlashAttribute("successMsg", "Slot removed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/interviewer/availability";
    }

    // ─── My Interviews ───────────────────────────────────────────────────────────

    @GetMapping("/interviews")
    public String myInterviews(@AuthenticationPrincipal User interviewer, Model model) {
        model.addAttribute("interviews", interviewService.findByInterviewer(interviewer));
        addNotifModel(model, interviewer);
        return "interviewer/interviews";
    }

    // ─── Feedback ────────────────────────────────────────────────────────────────

    @GetMapping("/feedback/{interviewId}")
    public String feedbackForm(@PathVariable Long interviewId,
                                @AuthenticationPrincipal User interviewer,
                                Model model) {
        model.addAttribute("feedbackDTO", new FeedbackDTO());
        model.addAttribute("interviewId", interviewId);
        model.addAttribute("results", com.shiksha.scheduler.model.FeedbackResult.values());
        interviewService.findById(interviewId).ifPresent(i -> model.addAttribute("interview", i));
        addNotifModel(model, interviewer);
        return "interviewer/feedback";
    }

    @PostMapping("/feedback")
    public String submitFeedback(@Valid @ModelAttribute("feedbackDTO") FeedbackDTO dto,
                                 BindingResult result,
                                 @AuthenticationPrincipal User interviewer,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (result.hasErrors()) {
            model.addAttribute("results", com.shiksha.scheduler.model.FeedbackResult.values());
            interviewService.findById(dto.getInterviewId()).ifPresent(i -> model.addAttribute("interview", i));
            addNotifModel(model, interviewer);
            return "interviewer/feedback";
        }
        try {
            interviewService.submitFeedback(dto, interviewer);
            redirectAttributes.addFlashAttribute("successMsg", "Feedback submitted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/interviewer/interviews";
    }

    private void addNotifModel(Model model, User user) {
        model.addAttribute("notifications", notificationService.getUnread(user));
        model.addAttribute("notifCount",    notificationService.countUnread(user));
        model.addAttribute("interviewer",   user);
    }
}
