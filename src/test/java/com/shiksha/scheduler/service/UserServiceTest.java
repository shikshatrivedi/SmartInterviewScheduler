package com.shiksha.scheduler.service;

import com.shiksha.scheduler.dto.RegisterDTO;
import com.shiksha.scheduler.exception.ConflictException;
import com.shiksha.scheduler.model.Role;
import com.shiksha.scheduler.model.User;
import com.shiksha.scheduler.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void register_Success() {
        // Arrange
        RegisterDTO dto = new RegisterDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("password");
        dto.setFullName("Test User");
        dto.setRole(Role.CANDIDATE);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        User result = userService.register(dto);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("hashed_password", result.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        // Arrange
        RegisterDTO dto = new RegisterDTO();
        dto.setEmail("existing@example.com");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> userService.register(dto));
        verify(userRepository, never()).save(any(User.class));
    }
}
