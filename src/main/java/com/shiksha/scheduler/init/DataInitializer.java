package com.shiksha.scheduler.init;

import com.shiksha.scheduler.model.Role;
import com.shiksha.scheduler.model.User;
import com.shiksha.scheduler.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUser("Admin User",    "admin@scheduler.com",       "Admin@123",      Role.ADMIN,       "Administration");
        seedUser("Sarah HR",      "hr@scheduler.com",          "Hr@12345",       Role.HR,          "Human Resources");
        seedUser("Rahul Sharma",  "interviewer@scheduler.com", "Interview@123",  Role.INTERVIEWER, "Engineering");
        seedUser("Priya Candidate","candidate@scheduler.com",  "Candidate@123",  Role.CANDIDATE,   null);
        log.info("✅ Default users seeded. Login: admin@scheduler.com / Admin@123");
    }

    private void seedUser(String name, String email, String password, Role role, String dept) {
        if (!userRepository.existsByEmail(email)) {
            userRepository.save(User.builder()
                    .fullName(name)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .role(role)
                    .department(dept)
                    .enabled(true)
                    .build());
        }
    }
}
