package com.shiksha.scheduler.controller;

import com.shiksha.scheduler.config.JwtUtils;
import com.shiksha.scheduler.config.SecurityConfig;
import com.shiksha.scheduler.dto.LoginDTO;
import com.shiksha.scheduler.dto.RegisterDTO;
import com.shiksha.scheduler.model.Role;
import com.shiksha.scheduler.model.User;
import com.shiksha.scheduler.service.LoginAttemptService;
import com.shiksha.scheduler.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private UserService userService;
    @Autowired private LoginAttemptService loginAttemptService;

    // ─── Login ───────────────────────────────────────────────────────────────────

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String denied,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null)  model.addAttribute("errorMsg", "Invalid email or password.");
        if (denied != null) model.addAttribute("errorMsg", "Access denied. Please log in again.");
        if (logout != null) model.addAttribute("successMsg", "You have been logged out successfully.");
        model.addAttribute("loginDTO", new LoginDTO());
        return "auth/login";
    }

    @PostMapping("/login")
    public String processLogin(@Valid @ModelAttribute("loginDTO") LoginDTO dto,
                               BindingResult result,
                               HttpServletRequest request,
                               HttpServletResponse response,
                               Model model) {
        if (result.hasErrors()) {
            return "auth/login";
        }

        String clientKey = SecurityConfig.getClientKey(request);

        // Rate limiting check
        if (loginAttemptService.isBlocked(clientKey)) {
            model.addAttribute("errorMsg",
                    "Too many failed attempts. Your access is blocked for 15 minutes.");
            return "auth/login";
        }
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));

            loginAttemptService.loginSucceeded(clientKey);

            String token = jwtUtils.generateToken(auth);

            Cookie cookie = new Cookie("JWT_TOKEN", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60); // 24 hours
            response.addCookie(cookie);

            // Update last login
            userService.findByEmail(dto.getEmail()).ifPresent(user -> {
                // lastLogin update via service can be added here
            });

            // Redirect based on role
            User user = (User) auth.getPrincipal();
            return switch (user.getRole()) {
                case ADMIN -> "redirect:/admin/dashboard";
                case HR -> "redirect:/hr/dashboard";
                case INTERVIEWER -> "redirect:/interviewer/dashboard";
                case CANDIDATE -> "redirect:/candidate/dashboard";
            };

        } catch (BadCredentialsException e) {
            loginAttemptService.loginFailed(clientKey);
            int remaining = loginAttemptService.getRemainingAttempts(clientKey);
            model.addAttribute("errorMsg",
                    "Invalid email or password. " + remaining + " attempt(s) remaining.");
            return "auth/login";
        }
    }

    // ─── Register ────────────────────────────────────────────────────────────────

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        model.addAttribute("roles", new Role[]{Role.CANDIDATE, Role.INTERVIEWER});
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("registerDTO") RegisterDTO dto,
                                  BindingResult result,
                                  Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", new Role[]{Role.CANDIDATE, Role.INTERVIEWER});
            return "auth/register";
        }
        // Public registration only for CANDIDATE and INTERVIEWER
        if (dto.getRole() == Role.ADMIN || dto.getRole() == Role.HR) {
            model.addAttribute("errorMsg", "Invalid role selection for self-registration.");
            model.addAttribute("roles", new Role[]{Role.CANDIDATE, Role.INTERVIEWER});
            return "auth/register";
        }
        try {
            userService.register(dto);
            return "redirect:/auth/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("roles", new Role[]{Role.CANDIDATE, Role.INTERVIEWER});
            return "auth/register";
        }
    }

    // ─── Logout ──────────────────────────────────────────────────────────────────

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("JWT_TOKEN", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Delete cookie
        response.addCookie(cookie);
        return "redirect:/auth/login?logout=true";
    }
}
