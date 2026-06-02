package com.shiksha.scheduler.dto;

import com.shiksha.scheduler.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegisterDTO {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Valid email is required")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;

    private String phoneNumber;
    private String department;

    public RegisterDTO() {}

    public String getFullName()           { return fullName; }
    public void   setFullName(String v)   { this.fullName = v; }
    public String getEmail()              { return email; }
    public void   setEmail(String v)      { this.email = v; }
    public String getPassword()           { return password; }
    public void   setPassword(String v)   { this.password = v; }
    public Role   getRole()               { return role; }
    public void   setRole(Role v)         { this.role = v; }
    public String getPhoneNumber()        { return phoneNumber; }
    public void   setPhoneNumber(String v){ this.phoneNumber = v; }
    public String getDepartment()         { return department; }
    public void   setDepartment(String v) { this.department = v; }
}
