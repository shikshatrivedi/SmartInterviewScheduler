package com.shiksha.scheduler.controller;

import com.shiksha.scheduler.dto.RegisterDTO;
import com.shiksha.scheduler.model.Role;
import com.shiksha.scheduler.service.InterviewService;
import com.shiksha.scheduler.service.JobService;
import com.shiksha.scheduler.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shiksha.scheduler.model.InterviewStatus;
import com.shiksha.scheduler.model.JobStatus;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private UserService userService;
    @Autowired private JobService jobService;
    @Autowired private InterviewService interviewService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers",       userService.findAll().size());
        model.addAttribute("totalHR",          userService.countByRole(Role.HR));
        model.addAttribute("totalInterviewers",userService.countByRole(Role.INTERVIEWER));
        model.addAttribute("totalCandidates",  userService.countByRole(Role.CANDIDATE));
        model.addAttribute("openJobs",         jobService.countByStatus(JobStatus.OPEN));
        model.addAttribute("scheduledInterviews", interviewService.countByStatus(InterviewStatus.SCHEDULED));
        model.addAttribute("completedInterviews", interviewService.countByStatus(InterviewStatus.COMPLETED));
        model.addAttribute("todayInterviews",  interviewService.countTodayInterviews());
        model.addAttribute("recentInterviews", interviewService.findAll().stream()
                .sorted((a, b) -> b.getScheduledAt().compareTo(a.getScheduledAt()))
                .limit(5).toList());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("registerDTO", new RegisterDTO());
        model.addAttribute("allRoles", Role.values());
        return "admin/users";
    }

    @PostMapping("/users/create")
    public String createUser(@Valid @ModelAttribute("registerDTO") RegisterDTO dto,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("users", userService.findAll());
            model.addAttribute("allRoles", Role.values());
            return "admin/users";
        }
        try {
            userService.register(dto);
            redirectAttributes.addFlashAttribute("successMsg", "User created successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleEnabled(id);
            redirectAttributes.addFlashAttribute("successMsg", "User status updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMsg", "User deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/interviews")
    public String allInterviews(Model model) {
        model.addAttribute("interviews", interviewService.findAll());
        return "admin/interviews";
    }
}
