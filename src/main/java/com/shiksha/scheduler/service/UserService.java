package com.shiksha.scheduler.service;

import com.shiksha.scheduler.dto.RegisterDTO;
import com.shiksha.scheduler.model.Role;
import com.shiksha.scheduler.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User register(RegisterDTO dto);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    List<User> findAll();
    List<User> findByRole(Role role);
    User updateUser(Long id, RegisterDTO dto);
    void toggleEnabled(Long id);
    void deleteUser(Long id);
    long countByRole(Role role);
}
